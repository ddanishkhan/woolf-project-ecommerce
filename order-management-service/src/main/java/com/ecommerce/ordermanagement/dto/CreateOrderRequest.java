package com.ecommerce.ordermanagement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO for requesting the creation of a new order.
 */
public record CreateOrderRequest(
        @NotEmpty List<OrderItemRequest> items
) {
}
