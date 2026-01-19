package com.bananabill.security;

import java.util.regex.Pattern;

/**
 * Mobile Number Validator
 * 
 * Validates Indian mobile numbers:
 * - 10 digits starting with 6, 7, 8, or 9
 * - Optional +91 or 91 prefix
 */
public final class MobileValidator {

    private MobileValidator() {
        // Utility class
    }

    /**
     * Valid Indian mobile number pattern
     * - Starts with 6, 7, 8, or 9
     * - Exactly 10 digits
     * - Optional +91 or 91 prefix
     */
    private static final Pattern INDIAN_MOBILE = Pattern.compile("^(?:\\+?91)?[6-9]\\d{9}$");

    /**
     * Validate mobile number
     */
    public static boolean isValid(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            return false;
        }
        // Remove spaces and dashes
        String cleaned = mobile.replaceAll("[\\s\\-]", "");
        return INDIAN_MOBILE.matcher(cleaned).matches();
    }

    /**
     * Clean and normalize mobile number to 10 digits
     */
    public static String normalize(String mobile) {
        if (mobile == null) {
            return null;
        }
        String cleaned = mobile.replaceAll("\\D", "");
        // Remove country code if present
        if (cleaned.startsWith("91") && cleaned.length() == 12) {
            cleaned = cleaned.substring(2);
        }
        return cleaned;
    }

    /**
     * Mask mobile number for logging (show last 4 only)
     */
    public static String mask(String mobile) {
        if (mobile == null || mobile.length() < 4) {
            return "****";
        }
        return "******" + mobile.substring(mobile.length() - 4);
    }
}
