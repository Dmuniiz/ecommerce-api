package com.api.e_commerce.product.dto;


import com.api.e_commerce.product.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String imageUrl,
        String description,
        String category,
        BigDecimal price,
        Integer stock,
        String status,
        Instant updateAt){

    public static ProductResponse fromEntity(Product p){
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getImageUrl(),
                p.getDescription(),
                p.getCategory().getName(),
                p.getPrice(),
                p.getStock(),
                p.getProductStatus().name(),
                p.getUpdatedAt()
        );
    }
}


