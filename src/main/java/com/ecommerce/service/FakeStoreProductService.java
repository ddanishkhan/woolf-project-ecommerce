package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.external.api.ProductStoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("fakeStore")
public class FakeStoreProductService implements ProductService {

    private final ProductStoreClient productStoreClient;

    @Autowired
    public FakeStoreProductService(@Qualifier("fakeStoreClient") ProductStoreClient productStoreClient) {
        this.productStoreClient = productStoreClient;
    }

    @Override
    public List<ProductResponse> getAllProducts() throws ProductNotFoundException {
        var response = productStoreClient.getAllProducts();
        if (response == null || response.getBody() == null) throw new ProductNotFoundException();
        return response.getBody();
    }

    @Override
    public ProductResponse getProductById(Integer id) throws ProductNotFoundException {
        var response = productStoreClient.getProductById(id);
        if (response == null || response.getBody() == null) throw new ProductNotFoundException();
        return response.getBody();
    }

    @Override
    public ProductResponse createNewProduct(ProductRequest productRequest) {
        return productStoreClient.createNewProduct(productRequest).getBody();
    }

    @Override
    public boolean deleteProductById(Integer id) {
        productStoreClient.deleteProductById(id);
        return true;
    }

    @Override
    public ProductResponse updateProductById(Integer id, ProductRequest productRequest) {
        return productStoreClient.updateProductById(id, productRequest).getBody();
    }

}
