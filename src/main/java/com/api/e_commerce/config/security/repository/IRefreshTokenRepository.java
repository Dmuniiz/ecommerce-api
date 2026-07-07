package com.api.e_commerce.config.security.repository;

import com.api.e_commerce.config.security.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying
    void deleteByUserId(UUID userId);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update RefreshToken token
               set token.revokedAt = :revokedAt
             where token.userId = :userId
               and token.revokedAt is null
            """)
    int revokeAllByUserId(UUID userId, Instant revokedAt);

    @Modifying
    @Query("""
            update RefreshToken token
               set token.revokedAt = :revokedAt
             where token.tokenFamilyId = :tokenFamilyId
               and token.revokedAt is null
            """)
    int revokeFamily(UUID tokenFamilyId, Instant revokedAt);
}
