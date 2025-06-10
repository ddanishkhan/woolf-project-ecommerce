package com.usermanagement.security;

import com.usermanagement.model.User;
import com.usermanagement.model.UserActiveToken;
import com.usermanagement.repository.UserActiveTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
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
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserActiveTokenRepository userActiveTokenRepository;
    @Value("${app.jwt.secret}")
    private String jwtSecretString;
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationInMs;
    @Getter
    private SecretKey jwtSecretKey;

    @PostConstruct
    protected void init() {
        byte[] keyBytes = jwtSecretString.getBytes();
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    @Transactional // Make token generation and saving active token transactional
    public String generateToken(User user, String name, Collection<? extends GrantedAuthority> authorities) {

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
                .claim("roles", roles)
                .claim("email", user.getEmail())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getJwtSecretKey(), Jwts.SIG.HS512)
                .compact();

        // Save the active token
        UserActiveToken activeToken = new UserActiveToken(jti, user, expiryDate);
        userActiveTokenRepository.save(activeToken);

        return tokenString;
    }

    // if User object might not be immediately available, (OAUTH), we need User to save UserActiveToken which does not occur here.
    public String generateToken(String subjectUsername, String name, Collection<? extends GrantedAuthority> authorities) {
        // cannot save to UserActiveToken unless we fetch the User here.
        // assume the caller will handle User fetching if using this. Or, this method could be deprecated/removed if User object is always available.
        log.warn("Warning: Generating token without saving to UserActiveToken table. User object needed.");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        String jti = UUID.randomUUID().toString();

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts
                .builder()
                .id(jti)
                .claim("name", name)
                .claim("roles", roles)
                .subject(subjectUsername)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getJwtSecretKey(), Jwts.SIG.HS512)
                .compact();
    }


    public String getUsernameFromJWT(String token) {
        return getAllClaimsFromToken(token).getSubject();
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
        List<String> roles = claims.get("roles", List.class);
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

}
