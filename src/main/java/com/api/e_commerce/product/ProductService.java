package com.api.e_commerce.product;

import com.api.e_commerce.config.exception.ValidationException;
import com.api.e_commerce.product.categories.ICategoryRepository;
import com.api.e_commerce.product.dto.CreateProductRequest;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final IProductRepository productRepository;
    private final ICategoryRepository categoryRepository;
    private final ProductSkuGenerator skuGenerator;


    @Transactional(readOnly = true)
    public Page<Product> findAllProductsForAdmin(ProductStatus status, Pageable pageable) {

        log.debug("Fetching all products for admin with status filter: {}", status);

        if (status != null) {
            return productRepository.findByProductStatus(status, pageable);
        }
        return productRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Product> findAllProductsForCustomer(Pageable pageable) {
        log.debug("Fetching available products for customer");
        return productRepository.findAllAvailable(pageable);
    }

    @Transactional
    public Product create(CreateProductRequest data) {
        log.info("Creating product: {}", data.name());
        
        if(productRepository.existsByNameIgnoreCase(data.name())) {
            log.warn("Product creation failed: duplicate name {}", data.name());
            throw new ValidationException("Product already exists");
        }
        
        String sku = skuGenerator.generateSku(data.name());
        var category = categoryRepository.getReferenceById(data.categoryId());
        
        Product product = new Product(data, category, sku);
        Product saved = productRepository.save(product);
        
        log.info("Product created successfully: {} with id: {}", data.name(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Product findByStringParamIdConvertToUUID(String id){
        try {
            UUID productId = UUID.fromString(id);
            return productRepository.findById(productId)
                    .orElseThrow(() -> new ValidationException("Product not found with id: " + id));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid UUID format");
        }
    }

    @Transactional
    public void updateProductStatus(Product product, ProductStatus status) {
        log.info("Updating product {} status from {} to {}", 
                product.getId(), product.getProductStatus(), status);
        
        if(product.getStock() <= 0 && status == ProductStatus.AVAILABLE) {
            log.warn("Cannot set AVAILABLE status for product with zero stock: {}", product.getId());
            throw new ValidationException("Product stock must be > 0 for AVAILABLE status");
        }

        product.changeStatus(status);
        productRepository.save(product);
        log.info("Product {} status updated to {}", product.getId(), status);
    }

    @Transactional
    public void deleteProductFromCatalog(Product product) {
        log.info("Deleting product from catalog: {}", product.getId());
        productRepository.delete(product);
        log.info("Product {} deleted successfully", product.getId());
    }

    public void decreaseStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
        log.debug("Decreasing stock for product {} by {} units", productId, quantity);
        int affected = productRepository.decreaseStock(productId, quantity);
        if (affected == 0) {
            log.warn("Failed to decrease stock for product {}: insufficient stock", productId);
            throw new ValidationException("Insufficient stock for product");
        }
    }

    public void increaseStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be positive");
        }
        log.debug("Increasing stock for product {} by {} units", productId, quantity);
        productRepository.increaseStock(productId, quantity);
        log.info("Stock increased for product {} by {} units", productId, quantity);
    }
}
