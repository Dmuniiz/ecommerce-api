package com.api.e_commerce.product.categories.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(

        @NotBlank
        String name,

        @NotBlank
        @Size(max = 100)
        String description

) { }
