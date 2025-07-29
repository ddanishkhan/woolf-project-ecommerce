package com.ecommerce.paymentservice.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderItemDto {
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal priceAtTimeOfOrder;
}
