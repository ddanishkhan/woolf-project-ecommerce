package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductListResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductListResponse getAllProducts(int page, int size);
    ProductResponse getProductById(UUID id)  throws ProductNotFoundException;
    ProductResponse getProductByNameFromDb(String productName)  throws ProductNotFoundException;
    ProductResponse createNewProduct(ProductRequest productRequest);
    boolean deleteProductById(UUID id) throws ProductNotFoundException;
    ProductResponse updateProductById(UUID id, ProductRequest productRequest)  throws ProductNotFoundException;

    // for Elasticsearch search
    List<ProductResponse> searchProductsByName(String name);
    List<ProductResponse> searchProductsByCategory(String category);
    List<ProductResponse> searchProductsByKeyword(String keyword);
}