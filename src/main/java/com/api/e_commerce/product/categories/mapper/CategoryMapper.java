package com.api.e_commerce.product.categories.mapper;

import com.api.e_commerce.product.categories.Category;
import com.api.e_commerce.product.categories.dto.CategoryResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    /**
     * Converts a Category entity to CategoryResponse DTO.
     *
     * @param category the category entity
     * @return CategoryResponse DTO
     */
    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt()
        );
    }

    /**
     * Converts a list of Category entities to CategoryResponse DTOs.
     *
     * @param categories list of category entities
     * @return list of CategoryResponse DTOs
     */
    public List<CategoryResponse> toResponseList(List<Category> categories) {
        return categories.stream()
                .map(this::toResponse)
                .toList();
    }
}

