package com.ecommerce.ordermanagement.events.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderReceiptGenerationEvent implements Serializable {
    private Long orderId;
    private String userEmail;
}
