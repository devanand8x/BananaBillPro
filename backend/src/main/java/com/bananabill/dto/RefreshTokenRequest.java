package com.bananabill.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh Token Request DTO
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required") String refreshToken) {
}
