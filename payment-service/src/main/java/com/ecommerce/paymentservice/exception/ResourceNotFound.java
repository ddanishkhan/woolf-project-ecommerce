package com.ecommerce.paymentservice.exception;

public class ResourceNotFound extends RuntimeException {
    public ResourceNotFound(String message, Throwable t) {
        super(message, t);
    }
}
