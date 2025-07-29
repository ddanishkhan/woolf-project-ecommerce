package com.ecommerce.ordermanagement.security;

import com.ecommerce.ordermanagement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Provides custom authorization logic for checking resource ownership.
 * This is used in @PreAuthorize annotations in the controller.
 */
@Service("orderSecurityService")
@RequiredArgsConstructor
public class OrderSecurityService {

    private final OrderRepository orderRepository;

    /**
     * Checks if the authenticated user is the owner of a specific order.
     *
     * @param authentication The Spring Security Authentication object.
     * @param orderId        The ID of the order to check.
     * @return true if the user is the owner, false otherwise.
     */
    public boolean isOwner(Authentication authentication, Long orderId) {
        if (authentication == null || !authentication.isAuthenticated() || !NumberUtils.isCreatable(authentication.getName())) {
            return false;
        }

        // The user's ID from the JWT principal
        final Long userId = Long.valueOf(authentication.getName());

        // Check the database to see if the customer ID on the order matches the user's ID
        return orderRepository.findById(orderId)
                .map(order -> order.getCustomer().getId().equals(userId))
                .orElse(false); // If order not found, access is denied
    }
}
