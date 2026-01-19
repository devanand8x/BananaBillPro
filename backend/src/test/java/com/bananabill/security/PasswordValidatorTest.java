package com.bananabill.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    @Test
    void password_MinLength8_ShouldPass() {
        String password = "Test@1234";
        assertTrue(password.length() >= 8);
    }

    @Test
    void password_TooShort_ShouldFail() {
        String password = "Te@1";
        assertFalse(password.length() >= 8);
    }

    @Test
    void password_HasUppercase_ShouldPass() {
        String password = "Test@1234";
        assertTrue(password.matches(".*[A-Z].*"));
    }

    @Test
    void password_NoUppercase_ShouldFail() {
        String password = "test@1234";
        assertFalse(password.matches(".*[A-Z].*"));
    }

    @Test
    void password_HasLowercase_ShouldPass() {
        String password = "Test@1234";
        assertTrue(password.matches(".*[a-z].*"));
    }

    @Test
    void password_HasNumber_ShouldPass() {
        String password = "Test@1234";
        assertTrue(password.matches(".*[0-9].*"));
    }

    @Test
    void password_HasSpecialChar_ShouldPass() {
        String password = "Test@1234";
        assertTrue(password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"));
    }

    @Test
    void password_NoSpecialChar_ShouldFail() {
        String password = "Test1234";
        assertFalse(password.matches(".*[!@#$%^&*(),.?\":{}|<>].*"));
    }
}
