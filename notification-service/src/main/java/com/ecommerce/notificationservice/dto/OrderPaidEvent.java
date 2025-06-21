package com.ecommerce.notificationservice.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderPaidEvent implements Serializable {
    private Long orderId;
    private String userEmail;
}


