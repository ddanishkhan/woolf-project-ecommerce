package com.usermanagement.exception;

public class RoleAlreadyExistsException extends RuntimeException {

    public RoleAlreadyExistsException(String roleName) {
        super(roleName);
    }

}
