package com.api.e_commerce.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks payment retry attempts for failed payments
 * Essential for handling transient gateway failures
 */
@Entity
@Table(name = "payment_retries", indexes = {
        @Index(name = "idx_payment_id", columnList = "payment_id"),
        @Index(name = "idx_next_retry_at", columnList = "next_retry_at")
})
@Getter
@Setter
@NoArgsConstructor
public class PaymentRetry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 3;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "last_error_message")
    private String lastErrorMessage;

    @Column(name = "is_retryable", nullable = false)
    private Boolean isRetryable = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public boolean isExhausted() {
        return attemptCount >= maxAttempts;
    }

    public boolean isReadyForRetry() {
        return isRetryable && !isExhausted() && 
               (nextRetryAt == null || Instant.now().isAfter(nextRetryAt));
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}

