package com.ecommerce.cartservice.controller;

import com.ecommerce.cartservice.dto.AddItemRequest;
import com.ecommerce.cartservice.dto.CartResponse;
import com.ecommerce.common.dtos.order.OrderResponse;
import com.ecommerce.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
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
        log.info("Get cart info for user: {}", userId);
        return ResponseEntity.ok(CartResponse.fromCart(cartService.getCartByUserId(userId)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasRole('SERVICE')")
    @GetMapping("/customer/{id}")
    public ResponseEntity<CartResponse> getCartByUser(@PathVariable String id) {
        log.info("Get cart info for user email: {}", id);
        return ResponseEntity.ok(CartResponse.fromCart(cartService.getCartByUserId(id)));
    }

    /**
     * Adds an item to the current user's cart.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(Authentication authentication, @RequestBody AddItemRequest request) {
        String userId = authentication.getName();
        log.info("Get cart items for user: {}", userId);
        return ResponseEntity.ok(CartResponse.fromCart(cartService.addItemToCart(userId, request)));
    }

    /**
     * Removes a specific item from the current user's cart.
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeItemFromCart(Authentication authentication, @PathVariable UUID productId) {
        String userId = authentication.getName();
        log.info("Remove cart product: {} for user: {}", productId, userId);
        return ResponseEntity.ok(CartResponse.fromCart(cartService.removeItemFromCart(userId, productId)));
    }

    /**
     * Clears all items from the current user's cart.
     */
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Clear cart for user: {}", userId);
        return ResponseEntity.ok(CartResponse.fromCart(cartService.clearCart(userId)));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal Jwt jwtPrincipal){
        String userId = jwtPrincipal.getSubject();
        var order = cartService.checkoutCart(userId, jwtPrincipal.getTokenValue());
        return ResponseEntity.ok(order);
    }
}
