package com.ecommerce.dtos.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        String productName,
        int quantity,
        BigDecimal priceAtTimeOfOrder
) {
}


