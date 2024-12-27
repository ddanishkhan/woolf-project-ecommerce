package com.ecommerce.service;

import com.ecommerce.dto.request.TokenAuthenticationRequest;
import com.ecommerce.external.api.auth.AuthenticationServer;
import com.ecommerce.external.api.auth.CustomJWTAuthentication;
import com.ecommerce.external.api.auth.JwtAuthenticationServer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.authorities.key}")
    public String authoritiesKey;

    private final AuthenticationServer authenticationServer;

    @Autowired
    public JwtService(JwtAuthenticationServer authenticationServer) {
        this.authenticationServer = authenticationServer;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && isTokenSessionValid(token);
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    public boolean isTokenSessionValid(String token) {
        final String username = extractUsername(token);
        var response = authenticationServer.validateToken(new TokenAuthenticationRequest(token, username));
        if (response.getBody() != null) {
            return response.getBody().sessionStatus().equals("ACTIVE");
        }
        log.error("Session status could not be validated {}", response.getBody());
        return false;
    }

    protected Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public CustomJWTAuthentication getAuthenticationToken(String token) {

        final JwtParser jwtParser = Jwts.parser()
                .verifyWith(getSignInKey())
                .build();

        final Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);

        final Claims claims = claimsJws.getPayload();

        String email = claims.getSubject();

        final Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(authoritiesKey).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        return new CustomJWTAuthentication(
                token,
                email,
                authorities
        );
    }
}