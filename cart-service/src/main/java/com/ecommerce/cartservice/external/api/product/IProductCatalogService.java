package com.ecommerce.cartservice.external.api.product;

import com.ecommerce.dtos.product.ProductResponse;

import java.util.UUID;

public interface IProductCatalogService {
    ProductResponse getProductDetail(UUID productId);
}
