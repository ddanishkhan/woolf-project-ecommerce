package com.ecommerce.external.api.auth;

import com.ecommerce.common.dtos.auth.TokenAuthenticationRequest;
import com.ecommerce.common.dtos.auth.TokenAuthenticationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class JwtAuthenticationServer implements AuthenticationServer {

    private final RestTemplateBuilder restTemplateBuilder;

    @Value("${security.jwt.server.url}")
    private String authServerUrl;

    private static final String AUTH = "/auth/validate-token";

    @Autowired
    public JwtAuthenticationServer(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Override
    public ResponseEntity<TokenAuthenticationResponse> validateToken(TokenAuthenticationRequest request) {
        String requestUrl = UriComponentsBuilder.fromUriString(authServerUrl).path(AUTH).build().toUriString();
        log.debug("Validate token via url: {}", requestUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + request.token());

        HttpEntity<TokenAuthenticationRequest> entity = new HttpEntity<>(request, headers);

        return restTemplateBuilder.build().exchange(requestUrl, HttpMethod.GET, entity, TokenAuthenticationResponse.class);
    }


}
