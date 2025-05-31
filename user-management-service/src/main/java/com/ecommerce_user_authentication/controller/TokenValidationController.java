package com.ecommerce_user_authentication.controller;

import com.ecommerce_user_authentication.security.JwtTokenProvider;
import com.ecommerce_user_authentication.service.TokenBlacklistService;
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
                    response.put("valid", false);
                    response.put("error", "Token has been revoked (blacklisted)");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }

                // Proceed with full validation if not blacklisted
                if (jwtTokenProvider.validateToken(jwt)) { // This will throw specific exceptions on failure
                    String username = jwtTokenProvider.getUsernameFromJWT(jwt);
                    List<GrantedAuthority> authorities = jwtTokenProvider.getAuthoritiesFromJWT(jwt);
                    List<String> roles = authorities.stream()
                            .map(GrantedAuthority::getAuthority)
                            .collect(Collectors.toList());

                    response.put("valid", true);
                    response.put("username", username);
                    response.put("authorities", roles);
                    // Optionally add JTI to response
                    response.put("jti", jti);
                    // Claims claims = jwtTokenProvider.getAllClaimsFromToken(jwt);
                    // response.put("issuedAt", claims.getIssuedAt());
                    // response.put("expiresAt", claims.getExpiration());

                    return ResponseEntity.ok(response);
                } else {
                    // This 'else' might be theoretically unreachable if validateToken always throws on failure
                    response.put("valid", false);
                    response.put("error", "Invalid token (unknown validation issue)");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            } catch (ExpiredJwtException ex) {
                response.put("valid", false);
                response.put("error", "Expired JWT token");
                response.put("jti", ex.getClaims().getId()); // JTI might still be available from expired token
                response.put("expiredAt", ex.getClaims().getExpiration());
            } catch (SignatureException ex) {
                response.put("valid", false);
                response.put("error", "Invalid JWT signature");
            } catch (MalformedJwtException ex) {
                response.put("valid", false);
                response.put("error", "Invalid JWT token (malformed)");
            } catch (UnsupportedJwtException ex) {
                response.put("valid", false);
                response.put("error", "Unsupported JWT token");
            } catch (IllegalArgumentException ex) { // empty or null token string to parser
                response.put("valid", false);
                response.put("error", "JWT claims string is empty or token is invalid.");
            } catch (JwtException ex) {
                response.put("valid", false);
                response.put("error", "JWT processing error: " + ex.getMessage());
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } else {
            response.put("valid", false);
            response.put("error", "Authorization header missing or does not contain Bearer token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
