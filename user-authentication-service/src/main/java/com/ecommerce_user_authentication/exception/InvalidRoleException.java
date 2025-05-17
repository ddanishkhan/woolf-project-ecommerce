package com.ecommerce_user_authentication.exception;

public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException() {
        super("Invalid role.");
    }

    public InvalidRoleException(String message) {
        super(message);
    }
}
