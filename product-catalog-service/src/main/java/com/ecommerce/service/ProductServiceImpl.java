package com.ecommerce.service;

import com.ecommerce.dto.CustomPageDTO;
import com.ecommerce.dto.StockUpdateRequest;
import com.ecommerce.dto.mapper.EntityToResponseMapper;
import com.ecommerce.dto.mapper.RequestToEntityMapper;
import com.ecommerce.dto.request.BatchStockUpdateRequest;
import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.dto.request.StockUpdateItem;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.elasticsearch.model.ProductDocument;
import com.ecommerce.elasticsearch.repository.ProductSearchRepository;
import com.ecommerce.events.dto.OrderItem;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.model.ProductEntity;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("productService")
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final CategoryRepository categoryRepository;

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository; // Elasticsearch repository

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              ProductSearchRepository productSearchRepository,
                              CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productSearchRepository = productSearchRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CustomPageDTO<ProductResponse> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // listing all products, from the primary DB (JPA)
        Page<ProductEntity> productEntityPage = productRepository.findAll(pageable);
        var res = EntityToResponseMapper.toProductResponse(productEntityPage.getContent());
        return new CustomPageDTO<>(res, productEntityPage);
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

        var category = categoryRepository.findById(productRequest.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("invalid category " + productRequest.categoryId()));

        var entity = RequestToEntityMapper.toProductEntity(productRequest, category);
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
        // Handle category update
        if (!existingEntity.getCategory().getId().equals(productRequest.categoryId())) {
            var category = categoryRepository.findById(productRequest.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found " + productRequest.categoryId()));
            existingEntity.setCategory(category);
        }

        ProductEntity savedEntity = productRepository.save(existingEntity);
        log.info("Product updated in DB with ID: {}", savedEntity.getId());

        // Update in Elasticsearch
        ProductDocument productDocument = ProductDocument.fromProductEntity(savedEntity);
        productSearchRepository.save(productDocument);
        log.info("Product updated in Elasticsearch with ID: {}", productDocument.getId());

        return EntityToResponseMapper.toProductResponse(savedEntity);
    }

    @Override
    @Transactional
    public void decrementStock(UUID productId, StockUpdateRequest request) {
        try {
            // 1. Find the product by its ID
            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

            // 2. Check for sufficient stock
            if (product.getStockQuantity() < request.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            // 3. Decrement the quantity
            product.setStockQuantity(product.getStockQuantity() - request.getQuantity());

            // 4. Save the product. JPA will use the @Version field automatically.
            // If another transaction has updated this product since we read it,
            // the version numbers won't match, and this will throw an OptimisticLockException.
            productRepository.save(product);

        } catch (OptimisticLockException ex) {
            // handles the overbooking race condition.
            throw new IllegalStateException("Could not update stock due to a concurrent modification. Please try again.");
        }
    }

    @Override
    @Transactional
    public void decrementStockBatch(BatchStockUpdateRequest request) {
        try {
            List<UUID> productIds = request.getItems().stream()
                    .map(StockUpdateItem::getProductId)
                    .toList();

            // 1. Fetch all requested products
            Map<UUID, ProductEntity> productsById = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(ProductEntity::getId, product -> product));

            // 2. Validate all items before making any changes.
            for (StockUpdateItem item : request.getItems()) {
                ProductEntity product = productsById.get(item.getProductId());
                if (product == null) {
                    throw new ProductNotFoundException("Product not found with ID: " + item.getProductId());
                }
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for product: " + product.getName());
                }
            }

            // 3. If all validations pass, perform the updates.
            for (StockUpdateItem item : request.getItems()) {
                ProductEntity product = productsById.get(item.getProductId());
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            }

            // 4. Save all modified products.
            // Atomic operation, If any save fails (e.g., OptimisticLockException),
            // all changes will be rolled back.
            productRepository.saveAll(productsById.values());

        } catch (OptimisticLockException ex) {
            throw new IllegalStateException("Could not update stock due to a concurrent modification. Please try again.");
        }
    }

    @Transactional
    @Override
    public void incrementStockBatch(BatchStockUpdateRequest request) {
        try {
            List<UUID> productIds = request.getItems().stream()
                    .map(StockUpdateItem::getProductId)
                    .toList();

            // 1. Fetch all products
            Map<UUID, ProductEntity> productsById = productRepository.findAllById(productIds).stream()
                    .collect(Collectors.toMap(ProductEntity::getId, product -> product));

            // 2. Validate all items before making any changes.
            for (StockUpdateItem item : request.getItems()) {
                ProductEntity product = productsById.get(item.getProductId());
                if (product == null) {
                    throw new ProductNotFoundException("Product not found with ID: " + item.getProductId());
                }
            }

            // 3. If all validations pass, perform the updates.
            for (StockUpdateItem item : request.getItems()) {
                ProductEntity product = productsById.get(item.getProductId());
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            }

            // 4. Save all modified products.
            // Atomic operation, If any save fails (e.g., OptimisticLockException), all changes will be rolled back.
            productRepository.saveAll(productsById.values());

        } catch (OptimisticLockException ex) {
            throw new IllegalStateException("Could not update stock due to a concurrent modification. Please try again.");
        }
    }

}
