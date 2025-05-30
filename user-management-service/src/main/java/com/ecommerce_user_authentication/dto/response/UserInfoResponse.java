package com.ecommerce_user_authentication.dto.response;

import com.ecommerce_user_authentication.model.RoleEntity;
import com.ecommerce_user_authentication.model.RoleEnum;
import com.ecommerce_user_authentication.model.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

public record UserInfoResponse(String email, Set<RoleEnum> roles) {

    public static UserInfoResponse from(UserEntity user) {
        return new UserInfoResponse(
                user.getEmail(),
                user.getRoleEntity().stream().map(RoleEntity::getName).collect(Collectors.toSet()));
    }

}
