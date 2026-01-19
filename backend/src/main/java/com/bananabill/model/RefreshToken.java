package com.bananabill.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Refresh Token Entity
 * Stores refresh tokens for JWT authentication
 * - Access Token: Short-lived (15 minutes)
 * - Refresh Token: Long-lived (7 days)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "refresh_tokens")
public class RefreshToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    @Indexed
    private String userId;

    private String mobileNumber;

    @Indexed(expireAfter = "0s") // TTL index - auto-delete expired tokens
    private Instant expiryDate;

    private Instant createdAt;

    private String userAgent;

    private String ipAddress;

    private boolean revoked = false;

    public RefreshToken(String token, String userId, String mobileNumber, Instant expiryDate) {
        this.token = token;
        this.userId = userId;
        this.mobileNumber = mobileNumber;
        this.expiryDate = expiryDate;
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
