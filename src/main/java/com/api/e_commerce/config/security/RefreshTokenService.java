package com.api.e_commerce.config.security;


import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.user.UserRepository;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public RefreshToken createRefreshToken(UUID userId){

        var user = userRepository.findById(userId).orElseThrow(() -> new ValidationException("User not found"));

        refreshTokenRepository.deleteByUser(user);

        var refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpirationTime(LocalDateTime.now().plusMinutes(60).toInstant(ZoneOffset.of("-03:00")));

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken refreshToken){

        var expiredOn = Instant.now();

        if(refreshToken.getExpirationTime().isBefore(expiredOn)){
            refreshTokenRepository.delete(refreshToken);

            throw new TokenExpiredException("Refresh token expired. Please login again.", expiredOn);
        }

        return refreshToken;
    }

    public RefreshToken findRefreshTokenByToken(String refreshToken){
        var oldToken = refreshTokenRepository.findByToken(refreshToken)
               .orElseThrow(() -> new ValidationException("Refresh token not found"));

        return verifyExpiration(oldToken);
    }


    @Transactional
    public void deleteByUserId(Long userId) {
        userRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }

}
