package com.bananabill.security;

import java.util.regex.Pattern;

/**
 * Password Policy Validator
 * 
 * Enforces strong password requirements:
 * - Minimum 8 characters
 * - At least 1 uppercase letter
 * - At least 1 lowercase letter
 * - At least 1 digit
 * - At least 1 special character
 */
public final class PasswordValidator {

    private PasswordValidator() {
        // Utility class
    }

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    // Patterns for password requirements
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*(),.?\":{}|<>\\[\\]\\-_+=;'/\\\\`~]");

    /**
     * Validate password and return validation result
     */
    public static ValidationResult validate(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.invalid("Password is required");
        }

        if (password.length() < MIN_LENGTH) {
            return ValidationResult.invalid("Password must be at least " + MIN_LENGTH + " characters");
        }

        if (password.length() > MAX_LENGTH) {
            return ValidationResult.invalid("Password must be at most " + MAX_LENGTH + " characters");
        }

        if (!HAS_UPPERCASE.matcher(password).find()) {
            return ValidationResult.invalid("Password must contain at least one uppercase letter");
        }

        if (!HAS_LOWERCASE.matcher(password).find()) {
            return ValidationResult.invalid("Password must contain at least one lowercase letter");
        }

        if (!HAS_DIGIT.matcher(password).find()) {
            return ValidationResult.invalid("Password must contain at least one digit");
        }

        if (!HAS_SPECIAL.matcher(password).find()) {
            return ValidationResult.invalid("Password must contain at least one special character (!@#$%^&*...)");
        }

        return ValidationResult.valid();
    }

    /**
     * Quick check if password is valid
     */
    public static boolean isValid(String password) {
        return validate(password).isValid();
    }

    /**
     * Validation result container
     */
    public record ValidationResult(boolean isValid, String error) {
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String error) {
            return new ValidationResult(false, error);
        }
    }
}
