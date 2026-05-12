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

    public Page<Product> findAllProductsPageable(ProductStatus status, Pageable pageable, Boolean isAdmin) {
        if(isAdmin) {
            if(status != null) {
                return productRepository.findByProductStatus(status, pageable);
            }else{
                productRepository.findAll(pageable);
            }
        }
        return productRepository.findAllAvailable(pageable);
    }

    //algorithm to generate standardized sku
    private String generateSku(String productName){

        // get the first 3 letters product
        String prefix = productName.substring(0, Math.min(productName.length(),3)).toUpperCase();
        prefix = prefix.replaceAll("[^A-Z0-9]", "X");

        String suffix = UUID.randomUUID().toString().substring(0 ,8).toUpperCase();

        return prefix + "-" + suffix;
    }

    @Transactional
    public Product create(CreateProductRequest data) {
        if(productRepository.existsByNameIgnoreCase(data.name())) {
            throw new ValidationException("Product already exists");
        }
        String sku = generateSku(data.name());

        var category = categoryRepository.getReferenceById(data.categoryId());

        Product product = new Product(data,category,sku);

        return productRepository.save(product);
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
        if(product.getStock() > 0) {
            product.changeStatus(status);
            productRepository.save(product);
        }

        log.info("Admin alterou status do produto {} para {}",  product.getId(), status);
    }

    @Transactional
    public void deleteProductFromCatalog(Product product) {
       productRepository.delete(product);
    }

    public void decreaseStock(UUID productId, int quantity) {
        productRepository.decreaseStock(productId, quantity);
    }
}
