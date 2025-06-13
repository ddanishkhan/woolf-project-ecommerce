package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.AddItemRequest;
import com.ecommerce.cartservice.dto.CartResponse;
import com.ecommerce.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * Retrieves the current user's shopping cart.
     * The user's identity is taken from the Authentication object provided by Spring Security.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(CartResponse.fromCart(cartService.getCartByUserId(userId)));
    }

    /**
     * Adds an item to the current user's cart.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(Authentication authentication, @RequestBody AddItemRequest request) {
        String userId = authentication.getName();
        return ResponseEntity.ok(CartResponse.fromCart(cartService.addItemToCart(userId, request)));
    }

    /**
     * Removes a specific item from the current user's cart.
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(Authentication authentication, @PathVariable String productId) {
        String userId = authentication.getName();
        return ResponseEntity.ok(CartResponse.fromCart(cartService.removeItemFromCart(userId, productId)));
    }

    /**
     * Clears all items from the current user's cart.
     */
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(CartResponse.fromCart(cartService.clearCart(userId)));
    }
}
