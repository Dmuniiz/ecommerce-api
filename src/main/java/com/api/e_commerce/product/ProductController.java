package com.api.e_commerce.product;

import com.api.e_commerce.product.categories.CategoryService;
import com.api.e_commerce.product.dto.CreateProductRequest;
import com.api.e_commerce.product.dto.ProductResponse;
import com.api.e_commerce.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request){
        var newProduct = productService.create(request);
        return ProductResponse.fromEntity(newProduct);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> patchUpdateStatus(@PathVariable String id, @RequestParam ProductStatus status){
        var product = productService.findByStringParamIdConvertToUUID(id);

        productService.updateProductStatus(product, status);

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable("/id") String id){
        var product = productService.findByStringParamIdConvertToUUID(id);

        return ResponseEntity.ok(ProductResponse.fromEntity(product));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProductsPageable(
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 12, sort = "name") Pageable pageable){

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        Page<Product> products = productService.findAllProductsPageable(status, pageable, isAdmin);

        var response = products.map(ProductResponse::fromEntity);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")//admin
    public ResponseEntity<Void> deleteProduct(@PathVariable String productId){

        var product = productService.findByStringParamIdConvertToUUID(productId);

        productService.deleteProductFromCatalog(product);

        return ResponseEntity.noContent().build();
    }

    //deleteProduct
    //throw new BusinessException("Produtos em rascunho precisam de descrição para serem publicados.");


}
