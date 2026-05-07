package com.api.e_commerce.config.security.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token")
})
@Setter
@Getter
public class RefreshToken {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Column(nullable = false)
    private Instant expirationTime;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

}
