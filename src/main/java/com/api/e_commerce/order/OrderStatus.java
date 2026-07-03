package com.api.e_commerce.order;

/**
 * Aligned order statuses for clearer mapping with PaymentStatus
 */
public enum OrderStatus {
    CREATED,           // Order created, payment not started
    PENDING_PAYMENT,   // Waiting for payment confirmation (Payment.PENDING)
    PAID,              // Payment confirmed (Payment.SUCCEEDED)
    PAYMENT_FAILED,    // Payment failed - in retry
    SHIPPED,           // Order shipped
    DELIVERED,         // Order delivered
    CANCELLED,         // Order cancelled
    REFUNDED           // Order refunded
}
