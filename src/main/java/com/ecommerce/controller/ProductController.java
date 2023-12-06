package com.ecommerce.controller;

import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(@Qualifier("fake store") ProductService productService){
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable Integer id) {
        return productService.getProductById(id);
    }

}
