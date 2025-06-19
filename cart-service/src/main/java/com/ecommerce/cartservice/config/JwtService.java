package com.ecommerce.cartservice.config;

import com.ecommerce.cartservice.dto.TokenAuthenticationRequest;
import com.ecommerce.cartservice.external.api.auth.AuthenticationServer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    private SecretKey jwtSecretKey;
    private final AuthenticationServer authenticationServer;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = secretKey.getBytes();
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extracts the username (subject) from the JWT token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    /**
     * Validates if the token is expired.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenSessionValid(String token) {

        if (isTokenExpired(token)) return false;

        final String username = extractUsername(token);
        var response = authenticationServer.validateToken(new TokenAuthenticationRequest(token, username));
        if (response.getBody() != null) {
            return response.getBody().valid();
        }
        log.error("Session status could not be validated {}", response.getBody());
        return false;
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}