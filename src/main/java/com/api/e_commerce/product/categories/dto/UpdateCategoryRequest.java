package com.api.e_commerce.product.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @NotBlank
        String name,

        @Size(max = 1000)
        String description
) {
}

