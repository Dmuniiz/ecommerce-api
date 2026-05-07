package com.api.e_commerce.config.security.services;


import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.config.security.model.RefreshToken;
import com.api.e_commerce.config.security.repository.IRefreshTokenRepository;
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

    private final IRefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(UUID userId){

        refreshTokenRepository.deleteByUserId(userId);

        var refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpirationTime(LocalDateTime.now().plusMinutes(60).toInstant(ZoneOffset.of("-03:00")));

        return refreshTokenRepository.save(refreshToken);
    }

    public void verifyExpiration(String refreshToken){
        var token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ValidationException("Refresh token not found"));

        if(token.getExpirationTime().isBefore(Instant.now())){
            refreshTokenRepository.delete(token);

            throw new TokenExpiredException("Refresh token expired. Please login again.", Instant.now());
        }
    }


    public UUID getUserIdByRefreshToken(String refreshToken) {
       return refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ValidationException("User not found"))
               .getUserId();
    }
}
