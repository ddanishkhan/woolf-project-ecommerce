package com.ecommerce.service;

import com.ecommerce.dto.mapper.EntityToResponseMapper;
import com.ecommerce.dto.mapper.RequestToEntityMapper;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("productService")
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductResponse> getAllProducts() throws ProductNotFoundException {
        var dbResponse =  productRepository.findAll();
        return EntityToResponseMapper.toProductResponse(dbResponse);
    }

    @Override
    public ProductResponse getProductById(Integer id) throws ProductNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProductResponse createNewProduct(ProductRequest productRequest) {
        var entity = RequestToEntityMapper.toProductEntity(productRequest);
        var savedEntity = productRepository.save(entity);
        return EntityToResponseMapper.toProductResponse(savedEntity);
    }

    @Override
    public boolean deleteProductById(Integer id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ProductResponse updateProductById(Integer id, ProductRequest productRequest) throws ProductNotFoundException {
        throw new UnsupportedOperationException();
    }
}
