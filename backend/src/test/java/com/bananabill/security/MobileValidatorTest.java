package com.bananabill.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MobileValidatorTest {

    @Test
    void mobile_ValidIndian10Digit_ShouldPass() {
        String mobile = "9876543210";
        assertTrue(mobile.matches("^[6-9]\\d{9}$"));
    }

    @Test
    void mobile_ValidStartsWith6_ShouldPass() {
        String mobile = "6123456789";
        assertTrue(mobile.matches("^[6-9]\\d{9}$"));
    }

    @Test
    void mobile_ValidStartsWith7_ShouldPass() {
        String mobile = "7123456789";
        assertTrue(mobile.matches("^[6-9]\\d{9}$"));
    }

    @Test
    void mobile_ValidStartsWith8_ShouldPass() {
        String mobile = "8123456789";
        assertTrue(mobile.matches("^[6-9]\\d{9}$"));
    }

    @Test
    void mobile_InvalidStartsWith5_ShouldFail() {
        String mobile = "5123456789";
        assertFalse(mobile.matches("^[6-9]\\d{9}$"));
    }

    @Test
    void mobile_TooShort_ShouldFail() {
        String mobile = "987654321";
        assertFalse(mobile.matches("^[6-9]\\d{9}$"));
    }

    @Test
    void mobile_TooLong_ShouldFail() {
        String mobile = "98765432101";
        assertFalse(mobile.matches("^[6-9]\\d{9}$"));
    }

    @Test
    void mobile_ContainsLetters_ShouldFail() {
        String mobile = "98765ABC10";
        assertFalse(mobile.matches("^[6-9]\\d{9}$"));
    }
}
