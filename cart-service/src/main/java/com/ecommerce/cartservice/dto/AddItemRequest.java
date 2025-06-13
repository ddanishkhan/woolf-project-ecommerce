package com.ecommerce.cartservice.dto;

import lombok.Data;

@Data
public class AddItemRequest {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
}