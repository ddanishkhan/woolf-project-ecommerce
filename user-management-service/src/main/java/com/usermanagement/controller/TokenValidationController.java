package com.usermanagement.controller;

import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class TokenValidationController {

    public static final String RESPONSE_KEY_VALID = "valid";
    public static final String RESPONSE_KEY_ERROR = "error";
    public static final String RESPONSE_KEY_USERNAME = "username";
    public static final String RESPONSE_KEY_AUTHORITIES = "authorities";
    public static final String RESPONSE_KEY_JTI = "jti";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public static String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        Map<String, Object> response = new HashMap<>();

        if (StringUtils.hasText(jwt)) {
            String jti = null;
            try {
                // Try to extract JTI first. If token is malformed, this might fail.
                jti = jwtTokenProvider.getJtiFromJWT(jwt);

                // Check blacklist
                if (tokenBlacklistService.isBlacklisted(jti)) {
                    response.put(RESPONSE_KEY_VALID, false);
                    response.put(RESPONSE_KEY_ERROR, "Token has been revoked (blacklisted)");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                // Proceed with full validation if not blacklisted
                if (jwtTokenProvider.validateToken(jwt)) {
                    String username = jwtTokenProvider.getUsernameFromJWT(jwt);
                    List<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromJWT(jwt);
                    List<String> roles = authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();

                    response.put(RESPONSE_KEY_VALID, true);
                    response.put(RESPONSE_KEY_USERNAME, username);
                    response.put(RESPONSE_KEY_AUTHORITIES, roles);
                    response.put(RESPONSE_KEY_JTI, jti);

                    return ResponseEntity.ok(response);
                } else {
                    // This 'else' might be theoretically unreachable if validateToken always throws on failure
                    response.put(RESPONSE_KEY_VALID, false);
                    response.put(RESPONSE_KEY_ERROR, "Invalid token (unknown validation issue)");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            } catch (ExpiredJwtException ex) {
                response.put(RESPONSE_KEY_VALID, false);
                response.put(RESPONSE_KEY_ERROR, "Expired JWT token");
                response.put(RESPONSE_KEY_JTI, ex.getClaims().getId());
                response.put("expiredAt", ex.getClaims().getExpiration());
            } catch (SignatureException ex) {
                response.put(RESPONSE_KEY_VALID, false);
                response.put(RESPONSE_KEY_ERROR, "Invalid JWT signature");
            } catch (MalformedJwtException ex) {
                response.put(RESPONSE_KEY_VALID, false);
                response.put(RESPONSE_KEY_ERROR, "Invalid JWT token (malformed)");
            } catch (UnsupportedJwtException ex) {
                response.put(RESPONSE_KEY_VALID, false);
                response.put(RESPONSE_KEY_ERROR, "Unsupported JWT token");
            } catch (IllegalArgumentException ex) { // empty or null token string to parser
                response.put(RESPONSE_KEY_VALID, false);
                response.put(RESPONSE_KEY_ERROR, "JWT claims string is empty or token is invalid.");
            } catch (JwtException ex) {
                response.put(RESPONSE_KEY_VALID, false);
                response.put(RESPONSE_KEY_ERROR, "JWT processing error: " + ex.getMessage());
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            response.put(RESPONSE_KEY_VALID, false);
            response.put(RESPONSE_KEY_ERROR, "Authorization header missing or does not contain Bearer token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
