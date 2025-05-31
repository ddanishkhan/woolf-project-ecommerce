package com.usermanagement.exception;

public class InvalidRoleException extends RuntimeException {
    public InvalidRoleException() {
        super("Invalid role.");
    }

    public InvalidRoleException(String message) {
        super(message);
    }
}
