package com.api.e_commerce.product.categories;

import com.api.e_commerce.product.categories.dto.CategoryResponse;
import com.api.e_commerce.product.categories.dto.CreateCategoryRequest;
import com.api.e_commerce.product.categories.dto.UpdateCategoryRequest;
import com.api.e_commerce.product.categories.mapper.CategoryMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestBody @Valid CreateCategoryRequest request,
            UriComponentsBuilder uriBuilder){
        var category = categoryService.create(request.name(), request.description());

        var uri = uriBuilder.path("/{categoryId}")
                .buildAndExpand(category.getId()).encode()
                .toUri();

        return ResponseEntity.created(uri).body(categoryMapper.toResponse(category));
    }

    @GetMapping("/{name}")
    public ResponseEntity<CategoryResponse> getCategoryByName(@PathVariable("name") String nameCategory){

        var category = categoryService.findByName(nameCategory);
        var response = categoryMapper.toResponse(category);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(){
        List<Category> categories = categoryService.findAllCategories();

        List<CategoryResponse> response = categories
                .stream()
                .map(categoryMapper::toResponse)
                .toList();

        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable("id") UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        var category = categoryService.update(categoryId, request);
        return ResponseEntity.ok(categoryMapper.toResponse(category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") UUID categoryId) {
        categoryService.delete(categoryId);
        return ResponseEntity.noContent().build();
    }
}
