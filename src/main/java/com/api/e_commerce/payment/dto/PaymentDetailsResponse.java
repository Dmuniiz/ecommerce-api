package com.api.e_commerce.payment.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Detailed payment status response
 */
public record PaymentDetailsResponse(
        String paymentId,
        String orderId,
        String status,
        String provider,
        BigDecimal amount,
        String currency,
        String failureReason,
        Instant paidAt,
        Instant createdAt,
        Instant updatedAt
) {}

