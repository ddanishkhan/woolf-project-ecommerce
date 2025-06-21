package com.ecommerce.paymentservice.exception;

public class ExternalClientException extends RuntimeException {
    public ExternalClientException(String message, Throwable e) {
        super(message, e);
    }
}
