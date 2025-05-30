package com.ecommerce.external.api.auth;

import com.ecommerce.dto.request.TokenAuthenticationRequest;
import com.ecommerce.dto.response.TokenAuthenticationResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationServer {
    ResponseEntity<TokenAuthenticationResponse> validateToken(TokenAuthenticationRequest request);
}
