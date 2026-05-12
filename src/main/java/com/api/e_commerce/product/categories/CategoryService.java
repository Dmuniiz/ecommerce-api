package com.api.e_commerce.product.categories;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.product.categories.dto.CreateCategoryRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ICategoryRepository categoryRepository;

    @Transactional
    public Category create(String name, String description) {
        if(categoryRepository.existsByNameCustom(name)){
            throw new ValidationException("Category already exists: " + name);
        }
        return categoryRepository.save(new Category(name, description));
    }


    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new ValidationException("category does not exist"));
    }

    @Transactional(readOnly = true)
    public List<Category> findAllCategories(){
        return categoryRepository.findAll();
    }


}
