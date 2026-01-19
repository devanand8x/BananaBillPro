package com.bananabill.security;

import java.util.regex.Pattern;

/**
 * Input Sanitizer - Prevents XSS and injection attacks
 * 
 * SECURITY:
 * - Strips HTML tags
 * - Removes script content
 * - Sanitizes special characters
 * - Validates input length
 */
public final class InputSanitizer {

    private InputSanitizer() {
        // Utility class
    }

    // Patterns for XSS detection
    private static final Pattern SCRIPT_TAG = Pattern.compile("<script[^>]*>.*?</script>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
    private static final Pattern JAVASCRIPT_PROTOCOL = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ON_EVENTS = Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE);

    /**
     * Sanitize text input - removes XSS vectors
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }

        String result = input;

        // Remove script tags and content
        result = SCRIPT_TAG.matcher(result).replaceAll("");

        // Remove HTML tags
        result = HTML_TAG.matcher(result).replaceAll("");

        // Remove javascript: protocol
        result = JAVASCRIPT_PROTOCOL.matcher(result).replaceAll("");

        // Remove on* event handlers
        result = ON_EVENTS.matcher(result).replaceAll("");

        // Trim whitespace
        result = result.trim();

        return result;
    }

    /**
     * Sanitize and validate length
     */
    public static String sanitize(String input, int maxLength) {
        String sanitized = sanitize(input);
        if (sanitized != null && sanitized.length() > maxLength) {
            return sanitized.substring(0, maxLength);
        }
        return sanitized;
    }

    /**
     * Check if input contains potential XSS
     */
    public static boolean containsXss(String input) {
        if (input == null) {
            return false;
        }
        return SCRIPT_TAG.matcher(input).find() ||
                JAVASCRIPT_PROTOCOL.matcher(input).find() ||
                ON_EVENTS.matcher(input).find();
    }

    /**
     * Sanitize for logging (mask sensitive data)
     */
    public static String sanitizeForLog(String input, int showChars) {
        if (input == null || input.length() <= showChars) {
            return input;
        }
        return input.substring(0, showChars) + "***";
    }

    /**
     * Escape for HTML display
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
