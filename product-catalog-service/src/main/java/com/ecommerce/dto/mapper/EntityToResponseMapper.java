package com.ecommerce.dto.mapper;

import com.ecommerce.common.dtos.product.ProductResponse;
import com.ecommerce.elasticsearch.model.ProductDocument;
import com.ecommerce.model.ProductEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class EntityToResponseMapper {

    private EntityToResponseMapper(){}

    // From ProductEntity (JPA) to ProductResponse
    public static ProductResponse toProductResponse(ProductEntity product) {
        if (product == null) return null;
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory() != null ? product.getCategory().getName() : "N/A")
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageURL(product.getCoverImageURL())
                .build();
    }

    // From List<ProductEntity> (JPA) to ProductListResponse
    public static List<ProductResponse> toProductResponse(List<ProductEntity> products) {
        if (products == null) return Collections.emptyList();
        return products.stream()
                .map(EntityToResponseMapper::toProductResponse)
                .toList();
    }

    // From ProductDocument (Elasticsearch) to ProductResponse
    public static ProductResponse toProductResponse(ProductDocument productDocument) {
        if (productDocument == null) return null;
        return ProductResponse.builder()
                .id(UUID.fromString(productDocument.getId())) // ID in document is UUID string
                .name(productDocument.getName())
                .category(productDocument.getCategory() != null ? productDocument.getCategory() : "N/A")
                .description(productDocument.getDescription())
                .price(productDocument.getPrice())
                .stockQuantity(productDocument.getStockQuantity())
                .imageURL(productDocument.getImageURL())
                .build();
    }

    // From List<ProductDocument> (Elasticsearch) to List<ProductResponse>
    public static List<ProductResponse> toProductResponseListFromDocuments(List<ProductDocument> productDocuments) {
        if (productDocuments == null) return new ArrayList<>();
        return productDocuments.stream()
                .map(EntityToResponseMapper::toProductResponse)
                .toList();
    }
}