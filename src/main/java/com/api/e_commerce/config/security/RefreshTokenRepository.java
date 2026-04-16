package com.api.e_commerce.config.security;

import com.api.e_commerce.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Modifying
    void deleteByUser(User user);

    Optional<RefreshToken> findByToken(String token);
}
