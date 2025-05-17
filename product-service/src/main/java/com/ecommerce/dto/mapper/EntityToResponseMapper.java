package com.ecommerce.dto.mapper;

import com.ecommerce.dto.response.ProductListResponse;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.model.ProductEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EntityToResponseMapper {

    public static ProductResponse toProductResponse(ProductEntity product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory().getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .imageURL(product.getCoverImageURL())
                .build();
    }

    public static ProductListResponse toProductResponse(List<ProductEntity> products) {
        var productListResponse = products.stream().map(EntityToResponseMapper::toProductResponse).collect(Collectors.toCollection(ArrayList::new));
        return new ProductListResponse(productListResponse);
    }

}