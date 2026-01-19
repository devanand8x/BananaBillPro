package com.bananabill.util;

import com.bananabill.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security Utilities - Common security helper methods
 * 
 * Centralized to avoid duplicate code across services
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // Utility class - no instantiation
    }

    /**
     * Get current authenticated user from SecurityContext
     * 
     * @return User object
     * @throws RuntimeException if not authenticated
     */
    public static User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Authentication required");
        }
        if (!(auth.getPrincipal() instanceof User)) {
            throw new RuntimeException("Invalid authentication principal");
        }
        return (User) auth.getPrincipal();
    }

    /**
     * Get current user ID
     */
    public static String getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Get current user mobile number
     */
    public static String getCurrentUserMobile() {
        return getCurrentUser().getMobileNumber();
    }

    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof User;
    }
}
