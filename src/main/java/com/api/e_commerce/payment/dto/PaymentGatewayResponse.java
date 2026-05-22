package com.api.e_commerce.payment.dto;

public record PaymentGatewayResponse(
        String checkoutUrl,
        String sessionId
) { }
