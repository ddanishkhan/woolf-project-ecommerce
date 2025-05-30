package com.ecommerce.service;

import com.ecommerce.dto.mapper.EntityToResponseMapper;
import com.ecommerce.dto.mapper.RequestToEntityMapper;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.response.ProductListResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.elasticsearch.model.ProductDocument;
import com.ecommerce.elasticsearch.repository.ProductSearchRepository;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.ProductEntity;
import com.ecommerce.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("productService")
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository; // Elasticsearch repository

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              ProductSearchRepository productSearchRepository) {
        this.productRepository = productRepository;
        this.productSearchRepository = productSearchRepository;
    }

    @Override
    public ProductListResponse getAllProducts() {
        // listing all products, from the primary DB (JPA)
        var dbResponse =  productRepository.findAll();
        return EntityToResponseMapper.toProductResponse(dbResponse);
    }

    @Override
    public ProductResponse getProductById(UUID id) throws ProductNotFoundException {
        var dbResponse = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        return EntityToResponseMapper.toProductResponse(dbResponse);
    }

    @Override
    public ProductResponse getProductByNameFromDb(String productName) throws ProductNotFoundException {
        var dbResponse = productRepository.findByNameContaining(productName)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with name containing: " + productName));
        return EntityToResponseMapper.toProductResponse(dbResponse);
    }

    @Override
    @Transactional // Ensures atomicity for DB save, Elasticsearch index, and Kafka send
    public ProductResponse createNewProduct(ProductRequest productRequest) {
        log.debug("Attempting to create new product: {}", productRequest.name());
        var entity = RequestToEntityMapper.toProductEntity(productRequest);
        // Ensure category is managed correctly if it's a separate entity and needs to be persisted or fetched.
        // For simplicity, assuming category name is directly set.

        ProductEntity savedEntity = productRepository.save(entity);
        log.info("Product saved to DB with ID: {}", savedEntity.getId());

        // Index in Elasticsearch
        ProductDocument productDocument = ProductDocument.fromProductEntity(savedEntity);
        productSearchRepository.save(productDocument);
        log.info("Product indexed in Elasticsearch with ID: {}", productDocument.getId());

        return EntityToResponseMapper.toProductResponse(savedEntity);
    }

    @Override
    @Transactional
    public boolean deleteProductById(UUID id) throws ProductNotFoundException {
        log.debug("Attempting to delete product with ID: {}", id);
        ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found for deletion with ID: " + id));

        String productName = productEntity.getName();

        productRepository.deleteById(id);
        log.info("Product deleted from DB with ID: {} name: {}", id, productName);

        // Delete from Elasticsearch
        productSearchRepository.deleteById(id.toString());
        log.info("Product deleted from Elasticsearch with ID: {}", id);

        return true;
    }

    @Override
    @Transactional
    public ProductResponse updateProductById(UUID id, ProductRequest productRequest) throws ProductNotFoundException {
        log.debug("Attempting to update product with ID: {}", id);
        // Check if product exists before updating.
        ProductEntity existingEntity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found for update with ID: " + id));

        // Update fields of existingEntity from productRequest
        // This avoids issues with detached entities or overwriting existing non-updated fields if RequestToEntityMapper creates a new one
        existingEntity.setName(productRequest.name());
        existingEntity.setDescription(productRequest.description());
        existingEntity.setPrice(productRequest.price());
        existingEntity.setCoverImageURL(productRequest.imageURL());
        // Handle category update - might need to fetch/create CategoryEntity if it's complex
        if (existingEntity.getCategory() == null || !existingEntity.getCategory().getName().equals(productRequest.category())) {
            com.ecommerce.model.CategoryEntity category = new com.ecommerce.model.CategoryEntity();
            category.setName(productRequest.category());
            existingEntity.setCategory(category); // Assuming cascade will handle saving category if new
        }

        ProductEntity savedEntity = productRepository.save(existingEntity);
        log.info("Product updated in DB with ID: {}", savedEntity.getId());

        // Update in Elasticsearch
        ProductDocument productDocument = ProductDocument.fromProductEntity(savedEntity);
        productSearchRepository.save(productDocument);
        log.info("Product updated in Elasticsearch with ID: {}", productDocument.getId());

        return EntityToResponseMapper.toProductResponse(savedEntity);
    }

    // --- Elasticsearch Search Methods ---

    @Override
    public List<ProductResponse> searchProductsByName(String name) {
        log.debug("Searching products by name in Elasticsearch: {}", name);
        List<ProductDocument> documents = productSearchRepository.findByNameContainingIgnoreCase(name);
        return documents.stream()
                .map(EntityToResponseMapper::toProductResponse) // Reusing existing mapper
                .toList();
    }

    @Override
    public List<ProductResponse> searchProductsByCategory(String category) {
        log.debug("Searching products by category in Elasticsearch: {}", category);
        List<ProductDocument> documents = productSearchRepository.findByCategory(category);
        return documents.stream()
                .map(EntityToResponseMapper::toProductResponse)
                .toList();
    }

    /**
     * This is a simple OR search on name and description.
     **/
    @Override
    public List<ProductResponse> searchProductsByKeyword(String keyword) {
        log.debug("Searching products by keyword in Elasticsearch: {}", keyword);
        List<ProductDocument> documents = productSearchRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
        return documents.stream()
                .map(EntityToResponseMapper::toProductResponse)
                .toList();
    }
}
