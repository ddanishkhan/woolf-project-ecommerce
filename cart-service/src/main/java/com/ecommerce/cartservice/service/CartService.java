package com.ecommerce.cartservice.service;


import com.ecommerce.cartservice.dto.AddItemRequest;
import com.ecommerce.cartservice.external.api.ordermanagement.OrderManagementService;
import com.ecommerce.cartservice.external.api.product.ProductCatalogService;
import com.ecommerce.cartservice.model.Cart;
import com.ecommerce.cartservice.model.CartItem;
import com.ecommerce.cartservice.repository.CartRepository;
import com.ecommerce.dtos.order.CreateOrderRequest;
import com.ecommerce.dtos.order.OrderItemRequest;
import com.ecommerce.dtos.order.OrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final ProductCatalogService productCatalogService;
    private final OrderManagementService orderManagementService;

    /**
     * Retrieves the cart for a given user.
     * Caches the result in Redis with the user's ID as the key.
     * If not in cache, fetches from MongoDB and then caches it.
     *
     * @param userId The user's unique ID (email).
     * @return The user's Cart.
     */
    @Cacheable(value = "carts", key = "#userId")
    public Cart getCartByUserId(String userId) {
        log.info("Fetching cart from MongoDB for user: {}", userId);
        return cartRepository.findByUserId(userId)
                .orElse(new Cart(userId)); // Return a new empty cart if none exists
    }

    /**
     * Adds an item to the user's cart.
     * Evicts the cart from the Redis cache to ensure data consistency.
     * The next call to getCartByUserId will fetch from DB and re-cache.
     *
     * @param userId  The user's unique ID (email).
     * @param request The item details to add.
     * @return The updated Cart.
     */
    @CacheEvict(value = "carts", key = "#userId")
    public Cart addItemToCart(String userId, AddItemRequest request) {
        log.debug("Adding item {} to cart for user: {}", request.getProductId(), userId);
        Cart cart = getCartFromDb(userId);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // If item exists, update its quantity
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            var product = productCatalogService.getProductDetail(request.getProductId());
            // If item is new, add it to the cart
            CartItem newItem = new CartItem(product.id(), product.name(), request.getQuantity(), product.price());
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    /**
     * Removes an item from the user's cart.
     * Evicts the cart from Redis cache.
     *
     * @param userId    The user's unique ID.
     * @param productId The ID of the product to remove.
     * @return The updated Cart.
     */
    @CacheEvict(value = "carts", key = "#userId")
    public Cart removeItemFromCart(String userId, UUID productId) {
        log.debug("Removing item {} from cart for user: {}", productId, userId);
        Cart cart = getCartFromDb(userId);
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        return cartRepository.save(cart);
    }


    /**
     * Clears all items from a user's cart.
     * Evicts the cart from Redis cache.
     *
     * @param userId The user's unique ID.
     * @return The cleared, empty Cart.
     */
    @CacheEvict(value = "carts", key = "#userId")
    public Cart clearCart(String userId) {
        log.debug("Clearing cart for user: {}", userId);
        Cart cart = getCartFromDb(userId);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    public OrderResponse checkoutCart(String userId, String token) {
        log.debug("Checkout the cart items.");
        var createOrder = createOrderFromCart(userId);
        var order = orderManagementService.createOrder(createOrder, token);
        log.info("order created : {}", order.orderId());
        return order;
    }

    private CreateOrderRequest createOrderFromCart(String userId) {
        List<OrderItemRequest> items = new ArrayList<>();
        var order = new CreateOrderRequest(items);
        var cart = getCartFromDb(userId);
        for (CartItem item : cart.getItems()) {
            items.add(new OrderItemRequest(item.getProductId(), item.getQuantity()));
        }
        return order;
    }

    /**
     * Helper method to fetch cart directly from the database, bypassing the cache.
     * @param userId The user's ID.
     * @return The Cart from MongoDB.
     */
    private Cart getCartFromDb(String userId) {
        return cartRepository.findByUserId(userId).orElse(new Cart(userId));
    }
}