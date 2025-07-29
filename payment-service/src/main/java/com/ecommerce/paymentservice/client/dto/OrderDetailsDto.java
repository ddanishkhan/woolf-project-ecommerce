package com.ecommerce.paymentservice.client.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Response from the Order-Management-Service
@Setter
@Getter
@ToString
public class OrderDetailsDto {
    private Long orderId;
    private Long customerId;
    private String customerName;
    private LocalDateTime orderDate;
    private String currency;
    private String status; // AWAITING_PAYMENT, PAYMENT_COMPLETE, etc.
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;

}
