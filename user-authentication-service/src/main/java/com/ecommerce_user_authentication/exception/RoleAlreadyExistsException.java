package com.ecommerce_user_authentication.exception;

public class RoleAlreadyExistsException extends RuntimeException {

    public RoleAlreadyExistsException(String roleName) {
        super(roleName);
    }

}
