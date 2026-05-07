package com.api.e_commerce.product.dto;


import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank
        String name,

        @NotBlank
        @URL
        String imageUrl,

        @Size(max = 500)
        String description,

        @NotNull
        UUID categoryId,

        @NotNull
        @Positive
        BigDecimal price,

        @NotNull
        @PositiveOrZero
        Integer stock
) {
}
