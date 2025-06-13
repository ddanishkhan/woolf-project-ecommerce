package com.ecommerce.cartservice.dto;

import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import lombok.Data;

import java.util.List;

/**
 * DTO for representing the user's cart in API responses.
 */
@Data
public class CartResponse {
    private String userId;
    private List<CartItem> items;
    private int totalItems;
    private double totalPrice;

    public static CartResponse fromCart(Cart cart) {
        CartResponse response = new CartResponse();
        response.setUserId(cart.getUserId());
        response.setItems(cart.getItems());
        response.setTotalItems(cart.getItems().stream().mapToInt(CartItem::getQuantity).sum());
        response.setTotalPrice(cart.getTotalPrice());
        return response;
    }
}
