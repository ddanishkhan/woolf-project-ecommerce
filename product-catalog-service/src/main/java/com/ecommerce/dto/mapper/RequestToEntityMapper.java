package com.ecommerce.dto.mapper;

import com.ecommerce.dto.request.ProductRequest;
import com.ecommerce.model.CategoryEntity;
import com.ecommerce.model.ProductEntity;

public class RequestToEntityMapper {

    private RequestToEntityMapper(){
        // static methods only
    }

    public static ProductEntity toProductEntity(ProductRequest product, CategoryEntity categoryEntity) {
        var productEntity = new ProductEntity();
        productEntity.setName(product.name());
        productEntity.setPrice(product.price());
        productEntity.setCategory(categoryEntity);
        productEntity.setDescription(product.description());
        productEntity.setCoverImageURL(product.imageURL());
        return productEntity;
    }

}
