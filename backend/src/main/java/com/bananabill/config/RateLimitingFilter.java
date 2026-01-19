package com.bananabill.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Rate limiting filter using Redis
 * Protects API endpoints from abuse
 */
@Component
@ConditionalOnProperty(name = "rate.limit.enabled", havingValue = "true", matchIfMissing = false)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Value("${rate.limit.global:100}")
    private int globalLimit;

    @Value("${rate.limit.auth:10}")
    private int authLimit;

    @Value("${rate.limit.window:60}")
    private int windowSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Skip rate limiting if Redis is not available
        if (redisTemplate == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = getClientId(request);
        String endpoint = request.getRequestURI();
        int limit;
        String key;

        // Apply stricter limits for auth endpoints
        if (endpoint.contains("/auth/login") || endpoint.contains("/auth/register")) {
            limit = authLimit;
            key = "rate_limit:auth:" + clientId;
        }
        // Apply strict limits for WhatsApp endpoints (prevent spam)
        else if (endpoint.contains("/whatsapp") || endpoint.contains("/send-bill") ||
                endpoint.contains("/send-statement") || endpoint.contains("/send-payment")) {
            limit = 5; // Only 5 WhatsApp messages per minute per IP
            key = "rate_limit:whatsapp:" + clientId;
        } else {
            limit = globalLimit;
            key = "rate_limit:global:" + clientId;
        }

        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String currentCount = ops.get(key);

        if (currentCount == null) {
            // First request in window
            ops.set(key, "1", Duration.ofSeconds(windowSeconds));
            filterChain.doFilter(request, response);
        } else {
            try {
                int count = Integer.parseInt(currentCount);
                if (count >= limit) {
                    // Rate limit exceeded
                    logger.warn("Rate limit exceeded for {} on {}", clientId, endpoint);
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.getWriter().write(String.format(
                            "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\",\"retryAfter\":%d}",
                            windowSeconds));
                    return;
                }

                // Increment counter
                ops.increment(key);
                filterChain.doFilter(request, response);
            } catch (NumberFormatException e) {
                // Invalid count, reset
                ops.set(key, "1", Duration.ofSeconds(windowSeconds));
                filterChain.doFilter(request, response);
            }
        }
    }

    private String getClientId(HttpServletRequest request) {
        // Use IP address as client identifier
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
