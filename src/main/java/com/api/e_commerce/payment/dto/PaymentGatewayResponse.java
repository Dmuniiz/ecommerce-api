package com.api.e_commerce.payment.dto;

/**
 * Response from payment gateway when creating checkout session
 */
public record PaymentGatewayResponse(
        String checkoutUrl,
        String sessionId
) {
    public String getSessionId() {
        return sessionId;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }
}
