package com.bananabill.service;

import com.bananabill.exception.ValidationException;
import com.bananabill.model.RefreshToken;
import com.bananabill.model.User;
import com.bananabill.repository.RefreshTokenRepository;
import com.bananabill.repository.UserRepository;
import com.bananabill.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Refresh Token Service
 * Handles refresh token creation, validation, and rotation
 */
@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final int MAX_SESSIONS_PER_USER = 5;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Create a new refresh token for a user
     */
    public RefreshToken createRefreshToken(String userId, String mobileNumber) {
        // Limit sessions per user
        long activeTokens = refreshTokenRepository.countByUserIdAndRevokedFalse(userId);
        if (activeTokens >= MAX_SESSIONS_PER_USER) {
            logger.warn("User {} has too many active sessions, revoking oldest", userId);
            revokeOldestToken(userId);
        }

        // Generate refresh token JWT
        String tokenString = jwtTokenProvider.generateRefreshToken(mobileNumber, userId);

        // Calculate expiry
        Instant expiryDate = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpiration());

        // Create and save
        RefreshToken refreshToken = new RefreshToken(tokenString, userId, mobileNumber, expiryDate);
        refreshToken = refreshTokenRepository.save(refreshToken);

        logger.info("Created refresh token for user: {}", maskMobile(mobileNumber));
        return refreshToken;
    }

    /**
     * Verify and return refresh token if valid
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByTokenAndRevokedFalse(token);
    }

    /**
     * Validate refresh token
     */
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            logger.warn("Revoked refresh token used: {}", refreshToken.getId());
            throw new ValidationException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new ValidationException("Refresh token has expired");
        }

        return refreshToken;
    }

    /**
     * Refresh tokens - generate new access token using refresh token
     * Implements token rotation for security
     */
    @Transactional
    public Map<String, String> refreshTokens(String refreshTokenString) {
        // Verify current refresh token
        RefreshToken oldRefreshToken = verifyRefreshToken(refreshTokenString);

        // Get user
        User user = userRepository.findById(oldRefreshToken.getUserId())
                .orElseThrow(() -> new ValidationException("User not found"));

        // Revoke old refresh token (rotation)
        oldRefreshToken.setRevoked(true);
        refreshTokenRepository.save(oldRefreshToken);

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getMobileNumber(), user.getId());

        RefreshToken newRefreshToken = createRefreshToken(
                user.getId(), user.getMobileNumber());

        logger.info("Tokens refreshed for user: {}", maskMobile(user.getMobileNumber()));

        return Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken.getToken(),
                "expiresIn", String.valueOf(jwtTokenProvider.getAccessTokenExpiration() / 1000));
    }

    /**
     * Revoke a specific refresh token (logout)
     */
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            logger.info("Revoked refresh token: {}", refreshToken.getId());
        });
    }

    /**
     * Revoke all tokens for a user (logout from all devices)
     */
    @Transactional
    public void revokeAllUserTokens(String userId) {
        refreshTokenRepository.findByUserIdAndRevokedFalse(userId).forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
        logger.info("Revoked all tokens for user: {}", userId);
    }

    /**
     * Get active session count for user
     */
    public long getActiveSessionCount(String userId) {
        return refreshTokenRepository.countByUserIdAndRevokedFalse(userId);
    }

    // ==================== HELPER METHODS ====================

    private void revokeOldestToken(String userId) {
        refreshTokenRepository.findByUserIdAndRevokedFalse(userId).stream()
                .min((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4)
            return "****";
        return "******" + mobile.substring(mobile.length() - 4);
    }
}
