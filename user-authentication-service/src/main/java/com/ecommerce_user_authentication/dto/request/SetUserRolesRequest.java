package com.ecommerce_user_authentication.dto.request;

import com.ecommerce_user_authentication.model.RoleEnum;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record SetUserRolesRequest(@NotNull Set<RoleEnum> roles) {
}
