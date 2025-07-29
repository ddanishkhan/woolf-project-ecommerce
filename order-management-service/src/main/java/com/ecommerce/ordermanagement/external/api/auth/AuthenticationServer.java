package com.ecommerce.ordermanagement.external.api.auth;

import com.ecommerce.common.dtos.auth.TokenAuthenticationRequest;
import com.ecommerce.common.dtos.auth.TokenAuthenticationResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationServer {
    ResponseEntity<TokenAuthenticationResponse> validateToken(TokenAuthenticationRequest request);
}
