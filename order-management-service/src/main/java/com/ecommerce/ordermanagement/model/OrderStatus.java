package com.ecommerce.ordermanagement.model;

/**
 * Represents the status of an order.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
