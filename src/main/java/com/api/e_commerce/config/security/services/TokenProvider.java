package com.api.e_commerce.config.security.services;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.user.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.issuer:ecommerce-api}")
    private String issuer;

    @Value("${jwt.audience:ecommerce-client}")
    private String audience;

    @Value("${jwt.access-token-expiration-minutes:15}")
    private long accessTokenExpirationMinutes;

    private final Clock clock = Clock.systemUTC();

    public String generateToken(User user){
        try {
            Instant now = clock.instant();
            Algorithm algorithm = Algorithm.HMAC256(validatedSecretKey());
            return JWT.create()
                    .withJWTId(UUID.randomUUID().toString())
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .withClaim("userId", user.getId().toString())
                    .withClaim("roles", extractRoles(user))
                    .withSubject(user.getUsername())
                    .withIssuedAt(now)
                    .withExpiresAt(expireAt())
                    .sign(algorithm);
        } catch (JWTCreationException jwtEx){
            throw new ValidationException("Failed to generate access JWT token");
        }
    }

    public String extractUsername(String token) {
        DecodedJWT decodedJWT;

        try {
            Algorithm algorithm = Algorithm.HMAC256(validatedSecretKey());

            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .withAudience(audience)
                    .build();

            decodedJWT = verifier.verify(token);

            return decodedJWT.getSubject();

        } catch (JWTVerificationException ex) {
            throw ex;
        }
    }

    public long getAccessTokenExpiresInSeconds() {
        return Duration.ofMinutes(accessTokenExpirationMinutes).toSeconds();
    }

    private Instant expireAt() {
        return clock.instant().plus(Duration.ofMinutes(accessTokenExpirationMinutes));
    }

    private List<String> extractRoles(User user) {
        return user.getRoles()
                .stream()
                .map(role -> role.getAuthority())
                .toList();
    }

    private String validatedSecretKey() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new ValidationException("JWT secret must contain at least 32 characters");
        }
        return secretKey;
    }

}
