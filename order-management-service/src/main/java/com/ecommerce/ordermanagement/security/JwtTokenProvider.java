package com.ecommerce.ordermanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    private final SecretKey jwtSecretKey;

    @Value("${app.jwt.secret}")
    private String jwtSecretString;


    // Read the secret from application.properties and create a SecretKey instance
    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret) {
        byte[] keyBytes = secret.getBytes();
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // Extract the User ID from the token's subject claim
    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Collection<SimpleGrantedAuthority> extractAuthorities(String token) {
        List<String> roles = extractClaim(token, claims -> claims.get("roles", List.class));
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    // Generic method to extract any claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Check if the token is valid (not expired and correctly signed)
    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Main parsing method that validates the signature
    private Claims extractAllClaims(String token) {
        return jwtParser()
                .parseSignedClaims(token)
                .getPayload();
    }

    private JwtParser jwtParser() {
        return Jwts.parser().verifyWith(jwtSecretKey).build();
    }

}
