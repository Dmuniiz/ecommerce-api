package com.api.e_commerce.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface IProductRepository extends JpaRepository<Product, UUID>  {
    Boolean existsByName(String name);

    Page<Product> findByProductStatus(ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.productStatus IN ('AVAILABLE','OUT_OF_STOCK')")
    Page<Product> findAllAvailable(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :stockQuantity WHERE p.id = :productId AND p.stock >= :stockQuantity")
    int decreaseStock(UUID productId, int stockQuantity);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :stockQuantity WHERE p.id = :productId")
    void increaseStock(UUID productId, int stockQuantity);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.id = :categoryId")
    boolean existsByCategory(UUID categoryId);
}
