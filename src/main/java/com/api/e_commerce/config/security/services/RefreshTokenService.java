package com.api.e_commerce.config.security.services;


import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.config.security.model.RefreshToken;
import com.api.e_commerce.config.security.repository.IRefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final IRefreshTokenRepository refreshTokenRepository;
    private final Clock clock = Clock.systemUTC();

    @Value("${jwt.refresh-token-expiration-minutes:10080}")
    private long refreshTokenExpirationMinutes;

    @Value("${jwt.secret}")
    private String tokenPepper;

    public record RefreshTokenResult(String token, RefreshToken entity) { }

    public record RefreshTokenMetadata(String deviceId, String ipAddress, String userAgent) { }

    @Transactional
    public RefreshTokenResult createRefreshToken(UUID userId, RefreshTokenMetadata metadata) {
        return createRefreshToken(userId, UUID.randomUUID(), metadata);
    }

    @Transactional
    public RefreshTokenResult rotateRefreshToken(String oldRawToken, RefreshTokenMetadata metadata) {
        RefreshToken currentToken = findByRawToken(oldRawToken);

        if (currentToken.isRevoked()) {
            refreshTokenRepository.revokeFamily(currentToken.getTokenFamilyId(), clock.instant());
            throw new BadCredentialsException("Refresh token reuse detected");
        }

        if (currentToken.isExpired()) {
            currentToken.setRevokedAt(clock.instant());
            refreshTokenRepository.save(currentToken);
            throw new CredentialsExpiredException("Refresh token expired. Please login again.");
        }

        currentToken.setRevokedAt(clock.instant());

        RefreshTokenResult replacement = createRefreshToken(
                currentToken.getUserId(),
                currentToken.getTokenFamilyId(),
                metadata
        );
        currentToken.setReplacedByTokenId(replacement.entity().getId());
        refreshTokenRepository.save(currentToken);

        return replacement;
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        RefreshToken token = findByRawToken(rawToken);
        if (!token.isRevoked()) {
            token.setRevokedAt(clock.instant());
            refreshTokenRepository.save(token);
        }
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, clock.instant());
    }

    public UUID getUserIdByRefreshToken(String rawToken) {
        RefreshToken token = findByRawToken(rawToken);
        if (token.isRevoked() || token.isExpired()) {
            throw new BadCredentialsException("Invalid refresh token");
        }
        return token.getUserId();
    }

    private RefreshTokenResult createRefreshToken(UUID userId, UUID tokenFamilyId, RefreshTokenMetadata metadata) {
        String rawToken = generateOpaqueToken();
        var refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setTokenFamilyId(tokenFamilyId);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpirationTime(clock.instant().plus(Duration.ofMinutes(refreshTokenExpirationMinutes)));

        if (metadata != null) {
            refreshToken.setDeviceId(metadata.deviceId());
            refreshToken.setIpAddress(metadata.ipAddress());
            refreshToken.setUserAgent(metadata.userAgent());
        }

        return new RefreshTokenResult(rawToken, refreshTokenRepository.save(refreshToken));
    }

    private RefreshToken findByRawToken(String rawToken) {
        return refreshTokenRepository.findByTokenHash(hashToken(rawToken))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
    }

    private String generateOpaqueToken() {
        return UUID.randomUUID() + "." + UUID.randomUUID();
    }

    private String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ValidationException("Refresh token is required");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((rawToken + tokenPepper).getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}
