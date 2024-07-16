package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.external.api.ProductStoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service("fakeStore")
public class FakeStoreProductService implements ProductService {

    private final ProductStoreClient productStoreClient;

    @Autowired
    public FakeStoreProductService(@Qualifier("fakeStoreClient") ProductStoreClient productStoreClient) {
        this.productStoreClient = productStoreClient;
    }

    @Override
    public List<ProductResponse> getAllProducts(){
        var response = productStoreClient.getAllProducts();
        if (response == null || response.getBody() == null) return List.of();
        return response.getBody();
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProductResponse getProductByName(String productName) throws ProductNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProductResponse createNewProduct(ProductRequest productRequest) {
        return productStoreClient.createNewProduct(productRequest).getBody();
    }

    @Override
    public boolean deleteProductById(UUID id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProductResponse updateProductById(UUID id, ProductRequest productRequest) {
        throw new UnsupportedOperationException();
    }

}
