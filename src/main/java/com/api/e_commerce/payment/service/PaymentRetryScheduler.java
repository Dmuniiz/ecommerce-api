package com.api.e_commerce.payment.service;

import com.api.e_commerce.payment.domain.PaymentRetry;
import com.api.e_commerce.payment.infrastructure.PaymentConfig;
import com.api.e_commerce.payment.repository.PaymentRetryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Scheduled service for retrying failed payments
 * Implements exponential backoff strategy for resilience
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentRetryScheduler {

    private final PaymentRetryRepository paymentRetryRepository;
    private final PaymentService paymentService;
    private final PaymentConfig paymentConfig;

    /**
     * Run payment retry task every 5 minutes
     * Retries failed payments with exponential backoff
     */
    @Scheduled(fixedDelayString = "${payment.retry.scheduler-interval-ms:300000}")
    public void retryFailedPayments() {
        if (!paymentConfig.isEnableWebhookRetry()) {
            return;
        }

        try {
            log.debug("Running payment retry scheduler");

            List<PaymentRetry> readyForRetry = paymentRetryRepository.findReadyForRetry(Instant.now());

            if (readyForRetry.isEmpty()) {
                log.debug("No payments ready for retry");
                return;
            }

            log.info("Found {} payments ready for retry", readyForRetry.size());

            for (PaymentRetry retry : readyForRetry) {
                try {
                    paymentService.retryFailedPayment(retry.getPaymentId());
                } catch (Exception e) {
                    log.error("Error retrying payment {}: {}", retry.getPaymentId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error in payment retry scheduler: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up exhausted retries periodically
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExhaustedRetries() {
        try {
            List<PaymentRetry> exhausted = paymentRetryRepository.findExhaustedRetries();

            if (!exhausted.isEmpty()) {
                log.info("Cleaning up {} exhausted payment retries", exhausted.size());
                // Could implement archival or notification here
            }
        } catch (Exception e) {
            log.error("Error cleaning up exhausted retries: {}", e.getMessage());
        }
    }
}

