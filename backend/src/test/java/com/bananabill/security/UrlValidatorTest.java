package com.bananabill.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    @Test
    void isValidImageUrl_NullUrl_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl(null));
    }

    @Test
    void isValidImageUrl_EmptyUrl_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl(""));
    }

    @Test
    void isValidImageUrl_BlankUrl_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("   "));
    }

    @Test
    void isValidImageUrl_HttpProtocol_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("http://example.com/image.png"));
    }

    @Test
    void isValidImageUrl_Localhost_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("https://localhost/image.png"));
    }

    @Test
    void isValidImageUrl_127001_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("https://127.0.0.1/image.png"));
    }

    @Test
    void isValidImageUrl_PrivateIp10_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("https://10.0.0.1/image.png"));
    }

    @Test
    void isValidImageUrl_PrivateIp192_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("https://192.168.1.1/image.png"));
    }

    @Test
    void isValidImageUrl_PrivateIp172_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("https://172.16.0.1/image.png"));
    }

    @Test
    void isValidImageUrl_UrlWithCredentials_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("https://user:pass@example.com/image.png"));
    }

    @Test
    void isValidImageUrl_InvalidUrl_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("not-a-valid-url"));
    }

    @Test
    void isValidImageUrl_JavascriptProtocol_ShouldReturnFalse() {
        assertFalse(UrlValidator.isValidImageUrl("javascript:alert(1)"));
    }

    @Test
    void validateImageUrl_InvalidUrl_ShouldThrowException() {
        assertThrows(SecurityException.class, () -> {
            UrlValidator.validateImageUrl("http://localhost/image.png");
        });
    }

    @Test
    void validateImageUrl_NullUrl_ShouldThrowException() {
        assertThrows(SecurityException.class, () -> {
            UrlValidator.validateImageUrl(null);
        });
    }
}
