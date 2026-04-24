package com.api.e_commerce.config.security;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.user.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(User user){
        // Implement token generation logic here (e.g., using JWT)
        try {
            Algorithm algorithm = Algorithm.HMAC256(encodedSecretKey());
            return JWT.create()
                    .withIssuer("auth-api")
                    .withAudience("ecommerce-client")
                    .withClaim("role", user.getRoles().getFirst().getAuthority())
                    .withSubject(user.getUsername())
                    .withIssuedAt(new Date(System.currentTimeMillis()))
                    .withExpiresAt(expireAt())
                    .sign(algorithm);
        } catch (JWTCreationException jwtEx){
            throw new ValidationException("Failed to generate access JWT token");
        }
    }

    public String extractSubject(String token) {
        DecodedJWT decodedJWT;

        try {
            Algorithm algorithm = Algorithm.HMAC256(encodedSecretKey());

            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth-api")
                    .withAudience("ecommerce-client")
                    .build();

            decodedJWT = verifier.verify(token);

            return decodedJWT.getSubject();

        } catch (JWTVerificationException ex) {
            throw new RuntimeException("Invalid or expired token: "+ ex.getMessage());
        }
    }

    private String encodedSecretKey() {
        return Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    private Instant expireAt() {
        return LocalDateTime.now().plusMinutes(15).toInstant(ZoneOffset.of("-03:00"));
    }



}
