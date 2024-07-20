package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductListResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(@Qualifier("productService") ProductService productService){
        this.productService = productService;
    }

    @GetMapping("/products")
    // This API does not fail, and returns empty list if no products are present.
    public ResponseEntity<ProductListResponse> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) throws ProductNotFoundException {
        var product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/products/name/{productName}")
    public ResponseEntity<ProductResponse> getProductByName(@PathVariable String productName) throws ProductNotFoundException {
        var product = productService.getProductByName(productName);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> addNewProduct(@RequestBody ProductRequest productRequestBody) {
        var product = productService.createNewProduct(productRequestBody);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProductDetail(@PathVariable UUID id, @RequestBody ProductRequest productRequest) throws ProductNotFoundException {
        var product = productService.updateProductById(id, productRequest);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Boolean> deleteProductById(@PathVariable UUID id) {
        var product = productService.deleteProductById(id);
        return ResponseEntity.ok(product);
    }

}
