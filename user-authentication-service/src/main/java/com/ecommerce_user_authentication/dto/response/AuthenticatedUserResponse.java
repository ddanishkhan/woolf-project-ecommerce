package com.ecommerce_user_authentication.dto.response;

import com.ecommerce_user_authentication.model.RoleEnum;

import java.util.Set;

public record AuthenticatedUserResponse(String email, Set<RoleEnum> roles) {
}