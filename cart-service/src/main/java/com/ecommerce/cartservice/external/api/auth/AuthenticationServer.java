package com.ecommerce.cartservice.external.api.auth;

import com.ecommerce.cartservice.dto.TokenAuthenticationRequest;
import com.ecommerce.cartservice.dto.TokenAuthenticationResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationServer {
    ResponseEntity<TokenAuthenticationResponse> validateToken(TokenAuthenticationRequest request);
}
