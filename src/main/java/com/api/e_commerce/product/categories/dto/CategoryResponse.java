package com.api.e_commerce.product.categories.dto;

import com.api.e_commerce.product.Product;
import com.api.e_commerce.product.categories.Category;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description,
        Instant createdAt
) {
    public static CategoryResponse fromEntity(Category category){
        return new CategoryResponse(
         category.getId(),
         category.getName(),
         category.getDescription(),
         category.getCreatedAt()
        );
    }
}
