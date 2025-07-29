package com.ecommerce.paymentservice.events.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
// Event received from the order service
public class OrderConfirmedEvent implements Serializable {
    private Long orderId;
    private BigDecimal totalAmount;
}
