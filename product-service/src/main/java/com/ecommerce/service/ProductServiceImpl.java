package com.ecommerce.service;

import com.ecommerce.dto.mapper.EntityToResponseMapper;
import com.ecommerce.dto.mapper.RequestToEntityMapper;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductListResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("productService")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductListResponse getAllProducts() {
        var dbResponse =  productRepository.findAll();
        return EntityToResponseMapper.toProductResponse(dbResponse);
    }

    @Override
    public ProductResponse getProductById(UUID id) throws ProductNotFoundException {
        var dbResponse = productRepository.findById(id).orElseThrow(ProductNotFoundException::new);
        return EntityToResponseMapper.toProductResponse(dbResponse);
    }

    @Override
    public ProductResponse getProductByName(String productName) throws ProductNotFoundException {
        var dbResponse = productRepository.findByNameContaining(productName).orElseThrow(ProductNotFoundException::new);
        return EntityToResponseMapper.toProductResponse(dbResponse);
    }

    @Override
    public ProductResponse createNewProduct(ProductRequest productRequest) {
        var entity = RequestToEntityMapper.toProductEntity(productRequest);
        var savedEntity = productRepository.save(entity);
        return EntityToResponseMapper.toProductResponse(savedEntity);
    }

    @Override
    public boolean deleteProductById(UUID id) {
        productRepository.deleteById(id);
        return true;
    }

    @Override
    public ProductResponse updateProductById(UUID id, ProductRequest productRequest) throws ProductNotFoundException {
        getProductById(id); // Check if product exists before updating.
        var entity = RequestToEntityMapper.toProductEntity(productRequest);
        entity.setId(id);
        var savedEntity = productRepository.save(entity);
        return EntityToResponseMapper.toProductResponse(savedEntity);
    }
}
