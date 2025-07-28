package com.ecommerce.common.dtos.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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

