package com.ecommerce.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException() {
        super();
    }

    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(UUID productId) {
        super("No product found for id: " + productId);
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}