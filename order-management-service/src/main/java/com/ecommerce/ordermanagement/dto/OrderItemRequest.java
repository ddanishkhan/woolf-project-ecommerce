package com.ecommerce.ordermanagement.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO for a single item within an order creation request.
 */
public record OrderItemRequest(
        @NotNull UUID productId,
        @Min(1) int quantity
) {
}
