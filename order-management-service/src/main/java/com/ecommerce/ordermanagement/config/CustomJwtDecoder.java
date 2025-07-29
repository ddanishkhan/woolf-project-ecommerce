package com.ecommerce.ordermanagement.config;

import com.ecommerce.common.dtos.auth.TokenAuthenticationRequest;
import com.ecommerce.common.dtos.auth.TokenAuthenticationResponse;
import com.ecommerce.ordermanagement.external.api.auth.AuthenticationServer;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final JWSVerifier verifier;
    private final AuthenticationServer authenticationServer;

    public CustomJwtDecoder(
            @Value("${security.jwt.secret-key}") String secret,
            AuthenticationServer authenticationServer) throws JOSEException {
        this.verifier = new MACVerifier(secret);
        this.authenticationServer = authenticationServer;
    }


    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);

            if (!signedJwt.verify(this.verifier)) {
                throw new JwtException("Invalid JWT signature.");
            }

            // Verify the token's expiration
            Instant expirationTime = signedJwt.getJWTClaimsSet().getExpirationTime().toInstant();
            if (Instant.now().isAfter(expirationTime)) {
                throw new JwtException("JWT expired at " + expirationTime);
            }

            String username = signedJwt.getJWTClaimsSet().getStringClaim("username");
            if (username == null) {
                throw new JwtException("JWT must contain a 'username' claim for session validation.");
            }

            // Verify the token's session via user-management.
            var request = new TokenAuthenticationRequest(token, username);
            ResponseEntity<TokenAuthenticationResponse> response = authenticationServer.validateToken(request);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || !response.getBody().valid()) {
                log.warn("Token session validation failed for user '{}'. Response status: {}, Body: {}",
                        username, response.getStatusCode(), response.getBody());
                throw new JwtException("Token session is no longer valid or could not be verified.");
            }
            log.debug("Token session successfully validated for user '{}'", username);


            // build the Spring Security Jwt object
            Map<String, Object> headers = signedJwt.getHeader().toJSONObject();
            Map<String, Object> claims = signedJwt.getJWTClaimsSet().toJSONObject();


            if (claims.get("iat") instanceof Long iat) {
                claims.put("iat", Instant.ofEpochSecond(iat));
            }
            if (claims.get("exp") instanceof Long exp) {
                claims.put("exp", Instant.ofEpochSecond(exp));
            }
            if (claims.get("nbf") instanceof Long nbf) {
                claims.put("nbf", Instant.ofEpochSecond(nbf));
            }

            return Jwt.withTokenValue(token)
                    .headers(h -> h.putAll(headers))
                    .claims(c -> c.putAll(claims))
                    .build();

        } catch (ParseException e) {
            throw new JwtException("Failed to parse the JWT.", e);
        } catch (JOSEException e) {
            throw new JwtException("Failed to verify the JWT signature.", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred during token validation", e);
            throw new JwtException("An unexpected error occurred during token validation.", e);
        }
    }

}


