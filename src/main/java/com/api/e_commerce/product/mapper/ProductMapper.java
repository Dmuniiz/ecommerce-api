package com.api.e_commerce.product.mapper;

import com.api.e_commerce.product.Product;
import com.api.e_commerce.product.dto.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Centralized mapper for Product entity to DTO conversions.
 * Separates concerns from DTOs and improves maintainability.
 */
@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getImageUrl(),
                product.getDescription(),
                product.getCategory().getName(),
                product.getPrice(),
                product.getStock(),
                product.getProductStatus().name(),
                product.getUpdatedAt()
        );
    }

    public List<ProductResponse> toResponseList(List<Product> products) {
        return products.stream()
                .map(this::toResponse)
                .toList();
    }

    public Page<ProductResponse> toResponsePage(Page<Product> products) {
        return products.map(this::toResponse);
    }
}

