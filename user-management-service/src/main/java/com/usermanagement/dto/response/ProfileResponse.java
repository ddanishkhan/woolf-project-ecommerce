package com.usermanagement.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
public class ProfileResponse {
    private String id;
    private String name;
    private String email;
    private String provider;
    private Set<String> roles;
}