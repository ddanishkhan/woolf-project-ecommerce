package com.ecommerce.common.dtos.auth;

public record TokenAuthenticationRequest(String token, String email) {
}
