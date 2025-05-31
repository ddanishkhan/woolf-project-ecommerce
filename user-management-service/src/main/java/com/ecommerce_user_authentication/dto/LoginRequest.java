package com.ecommerce_user_authentication.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
    private String usernameOrEmail;
    private String password;

}
