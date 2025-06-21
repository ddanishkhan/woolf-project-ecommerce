package com.ecommerce.ordermanagement.dto;

import com.ecommerce.ordermanagement.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for representing an order in API responses.
 */
public record OrderResponse(
        Long orderId,
        Long customerId,
        String customerName,
        LocalDateTime orderDate,
        String currency,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items
) {
}
