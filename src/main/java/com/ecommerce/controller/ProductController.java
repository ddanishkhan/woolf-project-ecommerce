package com.ecommerce.controller;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(@Qualifier("fakeStore") ProductService productService){
        this.productService = productService;
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() throws ProductNotFoundException {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Integer id) throws ProductNotFoundException {
        var product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> addNewProduct(@RequestBody ProductRequest productRequestBody) {
        var product = productService.createNewProduct(productRequestBody);
        return ResponseEntity.ok(product);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProductDetail(@PathVariable Integer id, @RequestBody ProductRequest productRequest) throws ProductNotFoundException {
        var product = productService.updateProductById(id, productRequest);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Boolean> deleteProductById(@PathVariable Integer id) {
        var product = productService.deleteProductById(id);
        return ResponseEntity.ok(product);
    }

}
