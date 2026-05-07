package com.api.e_commerce.config.security.repository;

import com.api.e_commerce.config.security.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;
import java.util.UUID;

public interface IRefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying
    void deleteByUserId(UUID userId);

    Optional<RefreshToken> findByToken(String token);
}
