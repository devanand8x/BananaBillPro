package com.bananabill.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Token Provider
 * Handles creation and validation of Access Tokens and Refresh Tokens
 * 
 * Token Strategy:
 * - Access Token: 15 minutes (short-lived, used for API calls)
 * - Refresh Token: 7 days (long-lived, used to get new access tokens)
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token.expiration:900000}") // 15 minutes default
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration:604800000}") // 7 days default
    private long refreshTokenExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== ACCESS TOKEN ====================

    /**
     * Generate Access Token (short-lived)
     */
    public String generateAccessToken(String mobileNumber, String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "ACCESS");

        return Jwts.builder()
                .claims(claims)
                .subject(mobileNumber)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generate Access Token (backward compatible - no userId)
     */
    public String generateToken(String mobileNumber) {
        return generateAccessToken(mobileNumber, null);
    }

    // ==================== REFRESH TOKEN ====================

    /**
     * Generate Refresh Token (long-lived)
     */
    public String generateRefreshToken(String mobileNumber, String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "REFRESH");

        return Jwts.builder()
                .claims(claims)
                .subject(mobileNumber)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    // ==================== TOKEN VALIDATION ====================

    /**
     * Validate any JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            logger.warn("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.debug("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.warn("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.warn("JWT claims string is empty");
        } catch (Exception ex) {
            logger.error("JWT validation error", ex);
        }
        return false;
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException ex) {
            return true;
        } catch (Exception ex) {
            return true;
        }
    }

    // ==================== EXTRACT CLAIMS ====================

    /**
     * Get mobile number from token
     */
    public String getMobileNumberFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Get user ID from token
     */
    public String getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return (String) claims.get("userId");
    }

    /**
     * Get token type (ACCESS or REFRESH)
     */
    public String getTokenType(String token) {
        Claims claims = getClaims(token);
        return (String) claims.get("type");
    }

    /**
     * Get expiration date from token
     */
    public Date getExpirationFromToken(String token) {
        return getClaims(token).getExpiration();
    }

    /**
     * Parse claims from token
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ==================== GETTERS FOR EXPIRATION ====================

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
