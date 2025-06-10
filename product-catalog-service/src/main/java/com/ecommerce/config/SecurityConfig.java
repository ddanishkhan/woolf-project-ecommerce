package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

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
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
