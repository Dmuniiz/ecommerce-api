package com.api.e_commerce.payment.repository;

import com.api.e_commerce.payment.domain.PaymentRetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRetryRepository extends JpaRepository<PaymentRetry, UUID> {
    Optional<PaymentRetry> findByPaymentId(UUID paymentId);

    @Query("SELECT pr FROM PaymentRetry pr WHERE pr.isRetryable = true " +
           "AND pr.attemptCount < pr.maxAttempts " +
           "AND (pr.nextRetryAt IS NULL OR pr.nextRetryAt <= :now) " +
           "ORDER BY pr.nextRetryAt ASC")
    List<PaymentRetry> findReadyForRetry(Instant now);

    @Query("SELECT pr FROM PaymentRetry pr WHERE pr.attemptCount >= pr.maxAttempts")
    List<PaymentRetry> findExhaustedRetries();
}

