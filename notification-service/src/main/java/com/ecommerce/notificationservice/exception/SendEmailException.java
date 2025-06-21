package com.ecommerce.notificationservice.exception;

public class SendEmailException extends RuntimeException {
    public SendEmailException(String message, Throwable t) {
        super(message, t);
    }
}
