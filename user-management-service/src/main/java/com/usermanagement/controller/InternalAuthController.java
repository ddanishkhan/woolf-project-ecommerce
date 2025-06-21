package com.usermanagement.controller;

import com.usermanagement.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/internal/auth")
public class InternalAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final String internalApiSecret;

    @Autowired
    public InternalAuthController(JwtTokenProvider jwtTokenProvider, @Value("${internal.api.secret}") String internalApiSecret) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.internalApiSecret = internalApiSecret;
    }

    @PostMapping("/service-token")
    public ResponseEntity<String> getServiceToken(@RequestHeader("X-Internal-Secret") String secret) {
        if (secret == null || !secret.equals(internalApiSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid secret key");
        }

        // Generate a token for a "service" user with a specific role
        String serviceUsername = "internal-notification-service";
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_SERVICE"));
        String token = jwtTokenProvider.generateInternalToken(serviceUsername, authorities);

        return ResponseEntity.ok(token);
    }
}
