package com.api.e_commerce.product.categories;

import com.api.e_commerce.product.categories.dto.CategoryResponse;
import com.api.e_commerce.product.categories.dto.CreateCategoryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
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

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody @Valid CreateCategoryRequest request, UriComponentsBuilder uriBuilder){
        var category = categoryService.create(request.name(), request.description());

        var uri = uriBuilder.path("/{name}")
                .buildAndExpand(category.getName())
                .encode()
                .toUri();

        return ResponseEntity.created(uri).body(CategoryResponse.fromEntity(category));
    }

    @GetMapping("/{name}")
    public ResponseEntity<CategoryResponse> getCategoryByName(@PathVariable("name") String nameCategory){

        var category = categoryService.findByName(nameCategory);
        var response = CategoryResponse.fromEntity(category);

        return ResponseEntity.ok().body(response);
    }


    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(){
        List<Category> categories = categoryService.findAllCategories();

        List<CategoryResponse> response = categories
                .stream()
                .map(c -> CategoryResponse.fromEntity(c))
                .toList();

        return ResponseEntity.ok().body(response);
    }


}
