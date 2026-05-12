package com.api.e_commerce.payment.domain.enums;

public enum PaymentTransactionType {
    CHECKOUT_SESSION_CREATED,
    PAYMENT_CONFIRMED,
    PAYMENT_FAILED,
    WEBHOOK_RECEIVED,
    REFUND_CREATED,
    REFUND_COMPLETED
}
