package com.ecommerce.service;

import com.ecommerce.dto.CustomPageDTO;
import com.ecommerce.dto.StockUpdateRequest;
import com.ecommerce.dto.request.BatchStockUpdateRequest;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;

import java.util.UUID;

public interface ProductService {
    CustomPageDTO<ProductResponse> getAllProducts(int page, int size);
    ProductResponse getProductById(UUID id)  throws ProductNotFoundException;
    ProductResponse getProductByNameFromDb(String productName)  throws ProductNotFoundException;
    ProductResponse createNewProduct(ProductRequest productRequest);
    boolean deleteProductById(UUID id) throws ProductNotFoundException;
    ProductResponse updateProductById(UUID id, ProductRequest productRequest)  throws ProductNotFoundException;
    void decrementStock(UUID productId, StockUpdateRequest request);
    void decrementStockBatch(BatchStockUpdateRequest request);
}