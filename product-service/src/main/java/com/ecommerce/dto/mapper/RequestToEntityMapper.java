package com.ecommerce.dto.mapper;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.model.CategoryEntity;
import com.ecommerce.model.ProductEntity;

public class RequestToEntityMapper {

    public static ProductEntity toProductEntity(ProductRequest product) {
        var category = new CategoryEntity();
        category.setName(product.category());

        var productEntity = new ProductEntity();
        productEntity.setName(product.name());
        productEntity.setPrice(product.price());
        productEntity.setCategory(category);
        productEntity.setDescription(product.description());
        productEntity.setCoverImageURL(product.imageURL());
        return productEntity;
    }

}
