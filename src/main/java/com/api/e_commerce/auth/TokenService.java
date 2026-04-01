package com.api.e_commerce.auth;

import com.api.e_commerce.config.exception.ValidationBusinessException;
import com.api.e_commerce.user.User;
import com.api.e_commerce.user.UserRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
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

    private final UserRepository userRepository;

    @Value("\"${jwt.secret}\"")
    private String secretKey;


    public String generateToken(User user){
        // Implement token generation logic here (e.g., using JWT)
        try {
            Algorithm algorithm = Algorithm.HMAC256(encodedSecretKey());
            return JWT.create()
                    .withIssuer("auth-api")
                    .withSubject(user.getUsername())
                    .withIssuedAt(new Date(System.currentTimeMillis()))
                    .withExpiresAt(expireAt())
                    .sign(algorithm);
        } catch (JWTCreationException jwtEx){
            throw new ValidationBusinessException("Failed to generate access JWT token");
        }
    }

    public User verifyToken(String token){
        // Implement token verification logic here (e.g., using JWT)
        DecodedJWT decodedJWT;
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth-api").build();

            decodedJWT = verifier.verify(token);
            String email = decodedJWT.getSubject();

            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ValidationBusinessException("User not found for the provided token"));

        } catch (Exception ex) {
            throw new ValidationBusinessException("Invalid or expired token");
        }
    }

    private String encodedSecretKey() {
        return Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    private Instant expireAt() {
        return LocalDateTime.now().plusMinutes(15).toInstant(ZoneOffset.of("-03:00"));
    }



}
