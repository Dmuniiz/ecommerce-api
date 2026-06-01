package com.api.e_commerce.payment.gateways;

import com.api.e_commerce.order.Order;
import com.api.e_commerce.payment.dto.PaymentGatewayResponse;

import java.math.BigDecimal;

/**
 * Strategy interface for payment gateway implementations
 * Supports multiple payment providers (Stripe, PayPal, etc.)
 */
public interface PaymentStrategy {

    /**
     * Create checkout session for payment
     */
    PaymentGatewayResponse createCheckoutSession(Order order);

    /**
     * Get provider name
     */
    String getProvider();

    /**
     * Convert amount to provider format (e.g., cents for Stripe)
     */
    Long ConvertToAmount(BigDecimal amount);

    /**
     * Refund a payment (optional - implement if provider supports it)
     */
    default String processRefund(String paymentIntentId, BigDecimal refundAmount) {
        throw new UnsupportedOperationException("Refunds not implemented for provider: " + getProvider());
    }
}
