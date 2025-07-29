package com.ecommerce.common.dtos.order;

public enum OrderStatus {
    PENDING,            // Initial state, before stock reservation
    CONFIRMED,
    AWAITING_PAYMENT,   // Stock reserved, waiting for user to pay
    PAID,               // Payment successful, ready for shipping
    PAYMENT_FAILED,     // An attempt to pay failed, user can retry
    CANCELLED,          // Order cancelled by user or system
    SHIPPED,            // Order has been shipped
    DELIVERED,          // Order has been delivered
    COMPLETED,           // Order finished
}


