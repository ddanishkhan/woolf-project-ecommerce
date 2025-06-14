package com.ecommerce.ordermanagement.dto;

import java.util.UUID;

public record OrderItem(UUID productId, int quantity) {
}
