package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;

import java.util.List;

public interface ProductService {
    List<ProductResponse> getAllProducts() throws ProductNotFoundException;
    ProductResponse getProductById(Integer id)  throws ProductNotFoundException;
    ProductResponse createNewProduct(ProductRequest productRequest);
    boolean deleteProductById(Integer id);
    ProductResponse updateProductById(Integer id, ProductRequest productRequest)  throws ProductNotFoundException;
}
