package com.ecommerce.cartservice.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's shopping cart.
 * mapped to the "carts" collection in MongoDB.
 */
@Data
@NoArgsConstructor
@Document(collection = "carts")
public class Cart implements Serializable {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // Corresponds to the user's email

    private List<CartItem> items = new ArrayList<>();

    public Cart(String userId) {
        this.userId = userId;
    }

    /**
     * Calculates the total price of all items in the cart.
     * @return The total price as a double, rounded to 2 decimal places.
     */
    public double getTotalPrice() {
        BigDecimal total = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}