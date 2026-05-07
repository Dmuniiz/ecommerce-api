package com.api.e_commerce.product.categories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ICategoryRepository extends JpaRepository<Category, UUID> {

    @Query(value = "SELECT EXISTS(SELECT 1 FROM categories WHERE name ILIKE %:name_category%)", nativeQuery = true)
    boolean existsByNameCustom(@Param("name_category") String nameCategory);

    Optional<Category> findByName(String name);
}

