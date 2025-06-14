package com.ecommerce.ordermanagement.dto;

import com.ecommerce.ordermanagement.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating the status of an order.
 */
public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status
) {
}
