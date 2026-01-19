package com.bananabill.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * URL Validator - Prevents SSRF (Server-Side Request Forgery) attacks
 * 
 * Validates URLs before passing to external services (Twilio, etc.)
 * 
 * SECURITY RULES:
 * 1. Only HTTPS allowed (production)
 * 2. Only whitelisted domains allowed
 * 3. No localhost, private IPs, or internal hosts
 * 4. No URL with authentication credentials
 */
public final class UrlValidator {

    private static final Logger logger = LoggerFactory.getLogger(UrlValidator.class);

    private UrlValidator() {
        // Utility class - no instantiation
    }

    /**
     * Allowed domains for image URLs (S3, Cloudinary, CDN, etc.)
     * Add trusted domains here
     */
    private static final Set<String> ALLOWED_DOMAINS = Set.of(
            "s3.amazonaws.com",
            "s3.ap-south-1.amazonaws.com",
            "cloudinary.com",
            "res.cloudinary.com",
            "images.unsplash.com",
            "cdn.bananabill.app",
            "storage.googleapis.com");

    /**
     * Private IP ranges that must be blocked (SSRF prevention)
     */
    private static final List<String> PRIVATE_IP_PREFIXES = List.of(
            "10.", // Class A private
            "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.",
            "172.24.", "172.25.", "172.26.", "172.27.",
            "172.28.", "172.29.", "172.30.", "172.31.", // Class B private
            "192.168.", // Class C private
            "127.", // Loopback
            "0.", // Invalid
            "169.254." // Link-local
    );

    private static final Pattern IPV6_LOOPBACK = Pattern.compile("^::1$|^0:0:0:0:0:0:0:1$");

    /**
     * Validate URL for external service use
     * 
     * @param urlString URL to validate
     * @return true if URL is safe to use
     */
    public static boolean isValidImageUrl(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(urlString);
            URL url = uri.toURL();

            // 1. Check protocol - HTTPS only
            if (!"https".equalsIgnoreCase(url.getProtocol())) {
                logger.warn("SSRF blocked: Non-HTTPS URL rejected: {}", sanitizeUrl(urlString));
                return false;
            }

            // 2. Check for credentials in URL
            if (url.getUserInfo() != null) {
                logger.warn("SSRF blocked: URL with credentials rejected");
                return false;
            }

            String host = url.getHost().toLowerCase();

            // 3. Check for localhost variants
            if (isLocalhost(host)) {
                logger.warn("SSRF blocked: Localhost URL rejected");
                return false;
            }

            // 4. Check for private IP ranges
            if (isPrivateIp(host)) {
                logger.warn("SSRF blocked: Private IP rejected");
                return false;
            }

            // 5. Resolve hostname and check resolved IP
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (addr.isLoopbackAddress() || addr.isSiteLocalAddress() ||
                        addr.isLinkLocalAddress() || addr.isAnyLocalAddress()) {
                    logger.warn("SSRF blocked: Resolved to internal IP: {}", host);
                    return false;
                }
            }

            // 6. Check against allowed domains
            if (!isAllowedDomain(host)) {
                logger.warn("SSRF blocked: Domain not in allowlist: {}", host);
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.warn("SSRF blocked: Invalid URL: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate and return URL or throw exception
     */
    public static String validateImageUrl(String urlString) {
        if (!isValidImageUrl(urlString)) {
            throw new SecurityException(
                    "Invalid or unsafe image URL. Only HTTPS URLs from trusted CDN domains are allowed.");
        }
        return urlString;
    }

    private static boolean isLocalhost(String host) {
        return host.equals("localhost") ||
                host.equals("127.0.0.1") ||
                host.equals("::1") ||
                host.equals("[::1]") ||
                host.endsWith(".local") ||
                host.endsWith(".internal");
    }

    private static boolean isPrivateIp(String host) {
        for (String prefix : PRIVATE_IP_PREFIXES) {
            if (host.startsWith(prefix)) {
                return true;
            }
        }
        return IPV6_LOOPBACK.matcher(host).matches();
    }

    private static boolean isAllowedDomain(String host) {
        for (String allowed : ALLOWED_DOMAINS) {
            if (host.equals(allowed) || host.endsWith("." + allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sanitize URL for logging (hide potential secrets)
     */
    private static String sanitizeUrl(String url) {
        if (url.length() > 100) {
            return url.substring(0, 100) + "...";
        }
        return url;
    }
}
