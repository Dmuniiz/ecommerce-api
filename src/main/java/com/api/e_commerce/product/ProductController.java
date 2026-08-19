package com.api.e_commerce.product;

import com.api.e_commerce.product.dto.CreateProductRequest;
import com.api.e_commerce.product.dto.ProductResponse;
import com.api.e_commerce.product.mapper.ProductMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request){
        var newProduct = productService.create(request);
        return productMapper.toResponse(newProduct);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> patchUpdateStatus(@PathVariable String id, @RequestParam ProductStatus status){
        var product = productService.findByStringParamIdConvertToUUID(id);

        productService.updateProductStatus(product, status);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable String id){
        var product = productService.findByStringParamIdConvertToUUID(id);

        return ResponseEntity.ok(productMapper.toResponse(product));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProductsPageable(
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 12, sort = "name") Pageable pageable){

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        Page<Product> products;

        if (isAdmin) {
            products = productService.findAllProductsForAdmin(status, pageable);
        } else {
            products = productService.findAllProductsForCustomer(pageable);
        }

        return ResponseEntity.ok(productMapper.toResponsePage(products));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId){

        var product = productService.findByStringParamIdConvertToUUID(productId);

        productService.deleteProductFromCatalog(product);

        return ResponseEntity.noContent().build();
    }

}
