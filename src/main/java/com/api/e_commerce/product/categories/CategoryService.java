package com.api.e_commerce.product.categories;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.product.IProductRepository;
import com.api.e_commerce.product.categories.dto.UpdateCategoryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final ICategoryRepository categoryRepository;
    private final IProductRepository productRepository;

    @Transactional
    public Category create(String name, String description) {
        log.info("Creating category: {}", name);

        if(categoryRepository.existsByNameCustom(name)){
            log.warn("Category creation failed: duplicate name {}", name);
            throw new ValidationException("Category already exists: " + name);
        }

        Category category = new Category(name, description);
        Category saved = categoryRepository.save(category);
        log.info("Category created successfully: {} with id: {}", name, saved.getId());

        return saved;
    }

    @Transactional(readOnly = true)
    public Category findByName(String name) {
        log.debug("Finding category by name: {}", name);
        return categoryRepository.findByName(name)
                .orElseThrow(() -> {
                    log.warn("Category not found with name: {}", name);
                    return new ValidationException("category does not exist");
                });
    }

    @Transactional(readOnly = true)
    public List<Category> findAllCategories(){
        log.debug("Fetching all categories");
        return categoryRepository.findAll();
    }

    @Transactional
    public Category update(UUID categoryId, UpdateCategoryRequest request) {
        log.info("Updating category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category not found with id: {}", categoryId);
                    return new ValidationException("Category not found");
                });

        // Check if name is already taken by another category
        if (!category.getName().equals(request.name()) &&
                categoryRepository.existsByNameCustom(request.name())) {
            log.warn("Category update failed: duplicate name {}", request.name());
            throw new ValidationException("Category name already exists: " + request.name());
        }

        category.update(request.name(), request.description());
        Category updated = categoryRepository.save(category);
        log.info("Category updated successfully: {} with id: {}", request.name(), categoryId);

        return updated;
    }

    @Transactional
    public void delete(UUID categoryId) {
        log.info("Deleting category: {}", categoryId);

        // Verify if there are products associated with this category
        if(productRepository.existsByCategory(categoryId)) {
            log.warn("Cannot delete category {} - has associated products", categoryId);
            throw new ValidationException("Cannot delete category - there are products associated with it");
        }

        categoryRepository.deleteById(categoryId);
        log.info("Category deleted successfully: {}", categoryId);
    }
}
