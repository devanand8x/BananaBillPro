package com.bananabill.dto;

/**
 * Token Response DTO
 * Returns both access and refresh tokens to the client
 */
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn, // Access token expiry in seconds
        UserDTO user) {
    public TokenResponse(String accessToken, String refreshToken, long expiresIn, UserDTO user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
