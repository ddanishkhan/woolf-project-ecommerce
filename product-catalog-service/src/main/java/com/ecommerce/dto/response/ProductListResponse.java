package com.ecommerce.dto.response;

import java.util.Collections;
import java.util.List;

public record ProductListResponse(List<ProductResponse> products, Integer totalPages, Long totalElements) {

    public static ProductListResponse empty() {
        return new ProductListResponse(Collections.emptyList(), 0, 0L);
    }

}
