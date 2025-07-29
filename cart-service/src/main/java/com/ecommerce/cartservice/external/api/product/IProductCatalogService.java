package com.ecommerce.cartservice.external.api.product;

import com.ecommerce.common.dtos.product.ProductResponse;

import java.util.UUID;

public interface IProductCatalogService {
    ProductResponse getProductDetail(UUID productId);
}
