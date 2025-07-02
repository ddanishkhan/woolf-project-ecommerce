package com.ecommerce.external.api;

import com.ecommerce.dtos.product.ProductRequest;
import com.ecommerce.dtos.product.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductStoreClient {
    ResponseEntity<List<ProductResponse>> getAllProducts();

    ResponseEntity<ProductResponse> getProductById(Integer id);

    ResponseEntity<ProductResponse> createNewProduct(ProductRequest productRequest);

    void deleteProductById(Integer id);

    ResponseEntity<ProductResponse> updateProductById(Integer id, ProductRequest productRequest);
}
