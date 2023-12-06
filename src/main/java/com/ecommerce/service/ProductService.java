package com.ecommerce.service;

import com.ecommerce.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Integer id);
}
