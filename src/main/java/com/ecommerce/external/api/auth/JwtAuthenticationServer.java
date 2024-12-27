package com.ecommerce.external.api.auth;

import com.ecommerce.dto.request.TokenAuthenticationRequest;
import com.ecommerce.dto.response.TokenAuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class JwtAuthenticationServer implements AuthenticationServer {

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.authorities.key}")
    private String authoritiesKey;

    @Value("${security.jwt.server.url}")
    private String authServerUrl;

    private static final String AUTH = "/auth/validate";

    @Autowired
    public JwtAuthenticationServer(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Override
    public ResponseEntity<TokenAuthenticationResponse> validateToken(TokenAuthenticationRequest request) {
        String requestUrl = UriComponentsBuilder.fromUriString(authServerUrl).path(AUTH).build().toUriString();
        return restTemplateBuilder.build().postForEntity(requestUrl, request, TokenAuthenticationResponse.class);
    }

}
