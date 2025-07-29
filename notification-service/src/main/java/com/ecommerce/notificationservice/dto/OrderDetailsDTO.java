package com.ecommerce.notificationservice.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Represents the data structure returned by the order-management-service
@Data
public class OrderDetailsDTO {
    private Long orderId;
    private Long customerId;
    private String customerName;
    private LocalDateTime orderDate;
    private String currency;
    private String status; // AWAITING_PAYMENT, PAYMENT_COMPLETE, etc.
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
}
