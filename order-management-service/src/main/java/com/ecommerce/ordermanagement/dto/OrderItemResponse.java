package com.ecommerce.ordermanagement.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for representing a single order item in API responses.
 */
public record OrderItemResponse(
        UUID productId,
        String productName,
        int quantity,
        BigDecimal priceAtTimeOfOrder
) {
}
