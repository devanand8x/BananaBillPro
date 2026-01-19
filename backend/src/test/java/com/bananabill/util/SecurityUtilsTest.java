package com.bananabill.util;

import com.bananabill.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityUtils
 */
class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        // Clear security context after each test
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return user when authenticated")
    void testGetCurrentUser_WhenAuthenticated() {
        // Given
        User mockUser = new User();
        mockUser.setId("user123");
        mockUser.setName("Test User");
        mockUser.setMobileNumber("9876543210");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null,
                Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // When
        User result = SecurityUtils.getCurrentUser();

        // Then
        assertNotNull(result);
        assertEquals("user123", result.getId());
        assertEquals("Test User", result.getName());
    }

    @Test
    @DisplayName("Should throw exception when not authenticated")
    void testGetCurrentUser_WhenNotAuthenticated() {
        // Given: No authentication set

        // When & Then
        assertThrows(RuntimeException.class, SecurityUtils::getCurrentUser);
    }

    @Test
    @DisplayName("Should return user ID correctly")
    void testGetCurrentUserId() {
        // Given
        User mockUser = new User();
        mockUser.setId("user456");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        String result = SecurityUtils.getCurrentUserId();

        // Then
        assertEquals("user456", result);
    }

    @Test
    @DisplayName("Should return mobile number correctly")
    void testGetCurrentUserMobile() {
        // Given
        User mockUser = new User();
        mockUser.setMobileNumber("9876543210");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When
        String result = SecurityUtils.getCurrentUserMobile();

        // Then
        assertEquals("9876543210", result);
    }

    @Test
    @DisplayName("Should return true when authenticated")
    void testIsAuthenticated_True() {
        // Given
        User mockUser = new User();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(mockUser, null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When & Then
        assertTrue(SecurityUtils.isAuthenticated());
    }

    @Test
    @DisplayName("Should return false when not authenticated")
    void testIsAuthenticated_False() {
        // Given: No authentication

        // When & Then
        assertFalse(SecurityUtils.isAuthenticated());
    }

    @Test
    @DisplayName("Should throw when principal is not User type")
    void testGetCurrentUser_WhenPrincipalIsString() {
        // Given
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("not-a-user", null,
                Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When & Then
        assertThrows(RuntimeException.class, SecurityUtils::getCurrentUser);
    }
}
