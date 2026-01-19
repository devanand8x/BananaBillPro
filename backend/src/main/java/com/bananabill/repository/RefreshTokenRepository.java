package com.bananabill.repository;

import com.bananabill.model.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for RefreshToken operations
 */
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    List<RefreshToken> findByUserId(String userId);

    List<RefreshToken> findByUserIdAndRevokedFalse(String userId);

    void deleteByUserId(String userId);

    void deleteByToken(String token);

    // For cleanup - find all revoked tokens
    List<RefreshToken> findByRevokedTrue();

    // Count active sessions for a user
    long countByUserIdAndRevokedFalse(String userId);
}
