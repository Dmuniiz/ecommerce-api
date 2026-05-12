package com.api.e_commerce.payment.dto;

public record CreateCheckoutSessionResponse(
        String checkoutUrl,
        String sessionId
) {
    public static CreateCheckoutSessionResponse fromEntity(String checkoutUrl, String sessionId) {
        return new CreateCheckoutSessionResponse(
                checkoutUrl, sessionId
        );
    }

}
