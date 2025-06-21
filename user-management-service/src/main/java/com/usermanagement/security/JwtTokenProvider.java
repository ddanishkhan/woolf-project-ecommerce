package com.usermanagement.security;

import com.usermanagement.model.User;
import com.usermanagement.model.UserActiveToken;
import com.usermanagement.repository.UserActiveTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String ROLES = "roles";
    private static final SecureDigestAlgorithm<SecretKey, ?> HS_512 = Jwts.SIG.HS512;

    private final UserActiveTokenRepository userActiveTokenRepository;

    @Value("${app.jwt.secret}")
    private String jwtSecretString;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;

    private SecretKey jwtSecretKey;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = jwtSecretString.getBytes();
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Transactional // Make token generation and saving active token transactional
    public String generateInternalToken(User user, String name, Collection<? extends GrantedAuthority> authorities) {

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        String jti = UUID.randomUUID().toString();

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String tokenString = Jwts
                .builder()
                .id(jti)
                .claim("name", name)
                .claim("username", user.getUsername())
                .claim(ROLES, roles)
                .claim("email", user.getEmail())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecretKey, HS_512)
                .compact();

        // Save the active token
        UserActiveToken activeToken = new UserActiveToken(jti, user, expiryDate);
        userActiveTokenRepository.save(activeToken);

        return tokenString;
    }

    public String generateInternalToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Date now = new Date();
        // Service tokens can have a longer validity
        Date expiryDate = new Date(now.getTime() + (jwtExpirationInMs * 10)); // e.g., 10x user expiration

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String jti = "srv_" + UUID.randomUUID().toString();
        return Jwts
                .builder()
                .id(jti)
                .claim("username", username)
                .claim(ROLES, roles)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecretKey, HS_512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }

    public String getJtiFromJWT(String token) {
        return getAllClaimsFromToken(token).getId();
    }

    public Date getExpirationDateFromJWT(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build().parseSignedClaims(token).getPayload();
    }

    public List<GrantedAuthority> getAuthoritiesFromJWT(String token) {
        Claims claims = getAllClaimsFromToken(token);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(ROLES, List.class);
        if (roles == null) {
            return List.of();
        }
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public boolean validateToken(String authToken) {
        Jwts.parser().verifyWith(jwtSecretKey).build().parseSignedClaims(authToken);
        return true;
    }


    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return (String) claims.get("email");
    }

    // Generic method to extract any claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

}
