package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductListResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For securing endpoints
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
    public ResponseEntity<ProductListResponse> getAllProducts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) throws ProductNotFoundException {
        var product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    // Directly search the DB-based name search. Only for debugging purpose.
    @Hidden
    @GetMapping("/products/name-db/{productName}")
    public ResponseEntity<ProductResponse> getProductByNameFromDb(@PathVariable String productName) throws ProductNotFoundException {
        var product = productService.getProductByNameFromDb(productName);
        return ResponseEntity.ok(product);
    }

    // Elasticsearch-powered search endpoints
    @GetMapping("/products/search")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam String keyword) {
        // This is a generic keyword search.
        List<ProductResponse> products = productService.searchProductsByKeyword(keyword);
        if (products.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/search/name")
    public ResponseEntity<List<ProductResponse>> searchProductsByName(@RequestParam String name) {
        List<ProductResponse> products = productService.searchProductsByName(name);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/search/category")
    public ResponseEntity<List<ProductResponse>> searchProductsByCategory(@RequestParam String category) {
        List<ProductResponse> products = productService.searchProductsByCategory(category);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }


    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> addNewProduct(@RequestBody ProductRequest productRequestBody) {
        var product = productService.createNewProduct(productRequestBody);
        return ResponseEntity.status(HttpStatus.CREATED).body(product); // Return 201 Created
    }

    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> updateProductDetail(@PathVariable UUID id, @RequestBody ProductRequest productRequest) throws ProductNotFoundException {
        var product = productService.updateProductById(id, productRequest);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProductById(@PathVariable UUID id) throws ProductNotFoundException {
        productService.deleteProductById(id);
        return ResponseEntity.noContent().build(); // Return 204 No Content
    }
}
