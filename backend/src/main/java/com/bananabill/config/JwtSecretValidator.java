package com.bananabill.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Validates JWT secret on application startup
 * Prevents running with default/insecure secrets
 */
@Component
@ConditionalOnProperty(name = "jwt.secret.validation.enabled", havingValue = "true", matchIfMissing = true)
public class JwtSecretValidator {

    private static final Logger logger = LoggerFactory.getLogger(JwtSecretValidator.class);

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @PostConstruct
    public void validateJwtSecret() {
        // Skip validation in test profile
        if ("test".equals(activeProfile)) {
            return;
        }

        // Check if secret is empty or default
        if (jwtSecret == null || jwtSecret.isEmpty() || 
            jwtSecret.equals("CHANGE_THIS_SECRET_IN_PRODUCTION_MIN_32_CHARS")) {
            String errorMsg = String.format(
                "❌ CRITICAL SECURITY ERROR: JWT secret is not set or uses default value!%n" +
                "   Set JWT_SECRET environment variable with at least 32 characters.%n" +
                "   Generate with: openssl rand -base64 64%n" +
                "   Application will not start in production without a valid secret."
            );
            logger.error(errorMsg);
            
            // Fail fast in production
            if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
                throw new IllegalStateException(
                    "JWT secret must be set via JWT_SECRET environment variable in production"
                );
            }
        } else if (jwtSecret.length() < 32) {
            logger.warn("⚠️  WARNING: JWT secret is less than 32 characters. Recommended: 64+ characters");
        } else {
            logger.info("✅ JWT secret validation passed (length: {})", jwtSecret.length());
        }
    }
}
