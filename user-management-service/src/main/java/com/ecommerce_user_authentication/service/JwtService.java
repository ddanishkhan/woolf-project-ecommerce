package com.ecommerce_user_authentication.service;

import com.ecommerce_user_authentication.exception.InvalidTokenException;
import com.ecommerce_user_authentication.model.SessionStatus;
import com.ecommerce_user_authentication.repository.SessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.validity}")
    private long jwtValidityDurationMs;

    @Value("${security.jwt.authorities.key}")
    public String authoritiesKey;

    private final SessionRepository sessionRepository;

    public JwtService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts
                .builder()
                .claim(authoritiesKey, authorities)
                .subject(authentication.getName())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtValidityDurationMs))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    public void validJWT(String token) {
        JwtParser jwtParser = Jwts.parser().verifyWith(getSignInKey()).build();
        try {
            jwtParser.parse(token);
        } catch (Exception e) {
            log.error("error validating jwt", e);
            throw new InvalidTokenException("Could not verify JWT token integrity!");
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token) && isTokenSessionValid(token);
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    private boolean isTokenSessionValid(String token) {
        final String username = extractUsername(token);
        var session = sessionRepository.findByTokenAndUser_Email(token, username);
        return session.filter(
                sessionEntity -> sessionEntity.getSessionStatus() == SessionStatus.ACTIVE
        ).isPresent();
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

    public UsernamePasswordAuthenticationToken getAuthenticationToken(final String token, final UserDetails userDetails) {

        final JwtParser jwtParser = Jwts.parser()
                .verifyWith(getSignInKey())
                .build();

        final Jws<Claims> claimsJws = jwtParser.parseSignedClaims(token);

        final Claims claims = claimsJws.getPayload();

        final Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(authoritiesKey).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .toList();
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

}
