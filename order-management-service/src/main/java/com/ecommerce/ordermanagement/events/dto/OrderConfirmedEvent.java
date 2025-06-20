package com.ecommerce.ordermanagement.events.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderConfirmedEvent {
    private Long orderId;
    private BigDecimal totalAmount;
    private String paymentMethodToken;
}
