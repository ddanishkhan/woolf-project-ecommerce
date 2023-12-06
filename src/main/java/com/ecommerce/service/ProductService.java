package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(Integer id);
    ProductResponse createNewProduct(ProductRequest productRequest);
    boolean deleteProductById(Integer id);
    ProductResponse updateProductById(Integer id, ProductRequest productRequest);
}
