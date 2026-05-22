package com.api.e_commerce.payment.dto;

import com.api.e_commerce.payment.domain.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;

public record CreateCheckoutRequest(
        @NotNull PaymentProvider provider
) {
}
