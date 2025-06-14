package com.ecommerce.ordermanagement.exception;

/**
 * Custom exception for order processing errors, such as insufficient stock.
 */
public class OrderProcessingException extends RuntimeException {
    public OrderProcessingException(String message) {
        super(message);
    }
}
