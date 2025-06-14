package com.ecommerce.ordermanagement.exception;

/**
 * Custom exception for handling generic resource not found errors.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
