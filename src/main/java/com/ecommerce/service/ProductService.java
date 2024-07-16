package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(UUID id)  throws ProductNotFoundException;
    ProductResponse getProductByName(String productName)  throws ProductNotFoundException;
    ProductResponse createNewProduct(ProductRequest productRequest);
    boolean deleteProductById(UUID id);
    ProductResponse updateProductById(UUID id, ProductRequest productRequest)  throws ProductNotFoundException;
}
