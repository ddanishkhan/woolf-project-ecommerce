package com.ecommerce.ordermanagement.model;

/**
 * Represents the status of an order.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    AWAITING_PAYMENT,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    PAID,
    PAYMENT_FAILED,
    CANCELLED
}
