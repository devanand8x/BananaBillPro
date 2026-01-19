package com.bananabill.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Set required properties using reflection
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "thisisaverylongsecretkeyfortestingpurposesmustbe32chars");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 604800000L);
    }

    @Test
    void generateAccessToken_ShouldReturnValidToken() {
        String token = jwtTokenProvider.generateAccessToken("9876543210", "user-1");

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.contains(".")); // JWT format: header.payload.signature
    }

    @Test
    void generateRefreshToken_ShouldReturnValidToken() {
        String token = jwtTokenProvider.generateRefreshToken("9876543210", "user-1");

        assertNotNull(token);
        assertTrue(token.contains("."));
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        String token = jwtTokenProvider.generateAccessToken("9876543210", "user-1");

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.here"));
    }

    @Test
    void validateToken_WithEmptyToken_ShouldReturnFalse() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    void getMobileNumberFromToken_ShouldReturnMobile() {
        String token = jwtTokenProvider.generateAccessToken("9876543210", "user-1");

        String mobile = jwtTokenProvider.getMobileNumberFromToken(token);

        assertEquals("9876543210", mobile);
    }

    @Test
    void getUserIdFromToken_ShouldReturnUserId() {
        String token = jwtTokenProvider.generateAccessToken("9876543210", "user-1");

        String userId = jwtTokenProvider.getUserIdFromToken(token);

        assertEquals("user-1", userId);
    }

    @Test
    void getTokenType_ForAccessToken_ShouldReturnACCESS() {
        String token = jwtTokenProvider.generateAccessToken("9876543210", "user-1");

        String type = jwtTokenProvider.getTokenType(token);

        assertEquals("ACCESS", type);
    }

    @Test
    void getTokenType_ForRefreshToken_ShouldReturnREFRESH() {
        String token = jwtTokenProvider.generateRefreshToken("9876543210", "user-1");

        String type = jwtTokenProvider.getTokenType(token);

        assertEquals("REFRESH", type);
    }

    @Test
    void isTokenExpired_WithValidToken_ShouldReturnFalse() {
        String token = jwtTokenProvider.generateAccessToken("9876543210", "user-1");

        assertFalse(jwtTokenProvider.isTokenExpired(token));
    }

    @Test
    void getExpirationFromToken_ShouldReturnFutureDate() {
        String token = jwtTokenProvider.generateAccessToken("9876543210", "user-1");

        Date expiration = jwtTokenProvider.getExpirationFromToken(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void getAccessTokenExpiration_ShouldReturn15Minutes() {
        long expiration = jwtTokenProvider.getAccessTokenExpiration();

        assertEquals(900000L, expiration); // 15 minutes in milliseconds
    }

    @Test
    void getRefreshTokenExpiration_ShouldReturn7Days() {
        long expiration = jwtTokenProvider.getRefreshTokenExpiration();

        assertEquals(604800000L, expiration); // 7 days in milliseconds
    }
}
