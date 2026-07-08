package com.api.e_commerce.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateOrderRequest(
        @NotNull
        UUID cartId,

        @NotNull
        UUID shippingAddressId,

        @NotNull
        UUID billingAddressId
) {
}
