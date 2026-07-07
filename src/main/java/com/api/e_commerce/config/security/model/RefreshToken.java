package com.api.e_commerce.config.security.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_refresh_token_family", columnList = "token_family_id")
})
@Setter
@Getter
public class RefreshToken {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "token_family_id", nullable = false, updatable = false)
    private UUID tokenFamilyId;

    @Column(nullable = false)
    private Instant expirationTime;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by_token_id")
    private Long replacedByTokenId;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    public boolean isExpired() {
        return expirationTime.isBefore(Instant.now());
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

}
