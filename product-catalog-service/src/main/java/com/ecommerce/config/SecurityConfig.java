package com.ecommerce.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    protected static final String[] PERMITTED_GET_URLS = {
            "/products/**",
            "/products/search/**",
            "/categories/**"
    };

    protected static final String[] PERMITTED_URLS = {
            "/swagger-ui/**",
            "/v3/api-docs*/**"
    };

    private final CustomJwtDecoder jwtDecoder;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF as we are using JWT and it's a stateless API
                .authorizeHttpRequests(authorize -> authorize
                        // Public GET endpoints for viewing products and searching
                        .requestMatchers(HttpMethod.GET, PERMITTED_GET_URLS).permitAll()
                        .requestMatchers(PERMITTED_URLS).permitAll()
                        // Other requests (POST, PUT, DELETE on /products) will be handled by @PreAuthorize
                        .anyRequest().authenticated() // All other requests need authentication
                )
                // session management is stateless, as JWT is used.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter()))
                );
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // This converter will extract authorities from the JWT.
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Tell the converter to look for the "roles" claim.
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");

        // Optional: If you want your authorities to be prefixed with "ROLE_",
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        // Create the main converter that will be used by the resource server.
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

}
