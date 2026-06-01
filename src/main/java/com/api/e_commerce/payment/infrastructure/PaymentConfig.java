package com.api.e_commerce.payment.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Payment configuration for e-commerce application
 * Centralizes timeout, retry, and idempotency settings
 */
@Component
@ConfigurationProperties(prefix = "payment")
@Getter
@Setter
public class PaymentConfig {

    // Timeout settings (in milliseconds)
    private long checkoutSessionTimeoutMs = 30000;
    private long refundTimeoutMs = 20000;
    private long webhookProcessingTimeoutMs = 10000;

    // Retry settings
    private int maxRetryAttempts = 3;
    private long initialBackoffMs = 1000;
    private long maxBackoffMs = 30000;
    private double backoffMultiplier = 2.0;

    // Idempotency settings
    private long idempotencyKeyExpirationHours = 24;
    private boolean enableIdempotencyValidation = true;

    // Webhook settings
    private boolean enableWebhookRetry = true;
    private int maxWebhookRetries = 5;
    private long webhookRetryIntervalMs = 5000;

    // Payment settings
    private boolean enableConcurrentPaymentPrevention = true;
    private long concurrentPaymentLockTimeoutSecs = 300;

    // Redirect URLs
    private String successUrl = "${app.frontend.url}/payment/success";
    private String cancelUrl = "${app.frontend.url}/payment/cancel";

    public long calculateBackoffDelay(int attemptNumber) {
        long delay = (long) (initialBackoffMs * Math.pow(backoffMultiplier, attemptNumber - 1));
        return Math.min(delay, maxBackoffMs);
    }
}

