package com.ecommerce.dto.mapper;

import com.ecommerce.dto.response.ProductListResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.elasticsearch.model.ProductDocument;
import com.ecommerce.model.ProductEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EntityToResponseMapper {

    private EntityToResponseMapper(){};

    // From ProductEntity (JPA) to ProductResponse
    public static ProductResponse toProductResponse(ProductEntity product) {
        if (product == null) return null;
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory() != null ? product.getCategory().getName() : "N/A")
                .description(product.getDescription())
                .price(product.getPrice())
                .imageURL(product.getCoverImageURL())
                .build();
    }

    // From List<ProductEntity> (JPA) to ProductListResponse
    public static ProductListResponse toProductResponse(List<ProductEntity> products, Integer totalPages, Long totalElements) {
        if (products == null) return new ProductListResponse(new ArrayList<>(), 0, 0L);
        var productListResponse = products.stream()
                .map(EntityToResponseMapper::toProductResponse)
                .collect(Collectors.toCollection(ArrayList::new));
        return new ProductListResponse(productListResponse, totalPages, totalElements);
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