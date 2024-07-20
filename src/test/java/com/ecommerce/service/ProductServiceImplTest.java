package com.ecommerce.service;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.CategoryEntity;
import com.ecommerce.model.ProductEntity;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    public void setup() {
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    void getAllProducts_Success() {
        CategoryEntity category = new CategoryEntity();
        ProductEntity product = new ProductEntity();
        product.setName("Mobile");
        product.setCategory(category);
        when(productRepository.findAll()).thenReturn(List.of(product));

        var result = productService.getAllProducts();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getProductById_Success() throws ProductNotFoundException {
        CategoryEntity category = new CategoryEntity();
        ProductEntity product = new ProductEntity();
        product.setId(UUID.randomUUID());
        product.setName("Mobile");
        product.setCategory(category);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(product.getId());
        assertNotNull(response);
        assertEquals(product.getId(), response.id());
        assertEquals(product.getName(), response.name());
    }

    @Test
    void createNewProduct_Success() {
        CategoryEntity category = new CategoryEntity();
        category.setName("electronics");
        ProductEntity product = new ProductEntity();
        product.setId(UUID.randomUUID());
        product.setName("Mobile");
        product.setCategory(category);

        var productRequest = new ProductRequest("Mobile", 1.0, "electronics", "", "");

        when(productRepository.save(any(ProductEntity.class))).thenReturn(product);

        ProductResponse response = productService.createNewProduct(productRequest);
        assertNotNull(response);
        assertEquals(product.getId(), response.id());
        assertEquals(product.getName(), response.name());
    }


    @Test
    void getProductByName_Success() throws ProductNotFoundException {
        CategoryEntity category = new CategoryEntity();
        ProductEntity product = new ProductEntity();
        product.setName("Mobile");
        product.setCategory(category);
        when(productRepository.findByNameContaining("Mobile")).thenReturn(Optional.of(product));

        var result = productService.getProductByName("Mobile");
        assertNotNull(result);
    }

    @Test
    void getProductByName_NoProduct() {
        when(productRepository.findByNameContaining("Mobile 2")).thenReturn(Optional.empty());

        var result = assertThrows(ProductNotFoundException.class, () -> productService.getProductByName("Mobile 2"));
        assertNull(result.getMessage());
    }

    @Captor
    ArgumentCaptor<ProductEntity> productEntityArgumentCaptor;

    @Test
    void updateProductById_Success() throws ProductNotFoundException {
        var productId = UUID.randomUUID();
        CategoryEntity category = new CategoryEntity();
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setName("Mobile");
        product.setCategory(category);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(product);
        var productRequest = new ProductRequest("Mobile - updated", 2.0, "electronics", "", "");
        ProductResponse response = productService.updateProductById(productId, productRequest);
        assertNotNull(response);

        verify(productRepository).save(productEntityArgumentCaptor.capture());
        var entityToSave = productEntityArgumentCaptor.getValue();
        assertEquals(productRequest.name(), entityToSave.getName());
        assertEquals(productRequest.price(), entityToSave.getPrice());
    }

}