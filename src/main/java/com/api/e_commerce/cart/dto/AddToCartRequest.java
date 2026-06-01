package com.api.e_commerce.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.UUID;

public record AddToCartRequest(
        @NotNull
        @Positive
        Integer quantity,

        @NotBlank
        @UUID
        String productId
) { }
