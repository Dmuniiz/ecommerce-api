package com.api.e_commerce.product;

import com.api.e_commerce.product.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    /*private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> createProduct(){

    }*/

}
