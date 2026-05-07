package com.api.e_commerce.order.dto;

import jakarta.validation.constraints.NotNull;

public record CheckoutOrderRequest(
        @NotNull
        String shippingAddressId,

        @NotNull
        String billingAddressId
) {
}
