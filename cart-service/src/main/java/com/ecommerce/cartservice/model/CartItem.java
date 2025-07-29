package com.ecommerce.cartservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single item within a shopping cart.
 * Implements Serializable to be cacheable by Redis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {
    private UUID productId;
    private String productName;
    private int quantity;
    private BigDecimal price; // Price per unit
}
