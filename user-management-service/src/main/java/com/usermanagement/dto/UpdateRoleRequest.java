package com.usermanagement.dto;

import com.usermanagement.model.ERole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoleRequest {
    private String email;
    private ERole role;
}
