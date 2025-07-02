package com.ecommerce.cartservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AddItemRequest {
    private UUID productId;
    private int quantity;
}