package com.ecommerce.dto.request;

public record TokenAuthenticationRequest(String token, String email) {
}
