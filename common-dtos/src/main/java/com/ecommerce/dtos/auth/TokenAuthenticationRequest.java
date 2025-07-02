package com.ecommerce.dtos.auth;

public record TokenAuthenticationRequest(String token, String email) {
}
