package com.ecommerce.config;

import com.ecommerce.exception.AuthenticationException;
import com.ecommerce.external.api.auth.CustomJWTAuthentication;
import com.ecommerce.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;

    public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver, JwtService jwtService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.error("Bearer token missing.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7); //Token present after bearer
            final String userEmail = jwtService.extractUsername(jwt);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (userEmail != null && authentication == null) {

                if (jwtService.isTokenSessionValid(jwt)) {
                    CustomJWTAuthentication authToken = jwtService.getAuthenticationToken(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    throw new AuthenticationException("Unauthorized");
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            log.error("Exception ", exception);
            //forward the error to the global exception handler.
            handlerExceptionResolver.resolveException(request, response, null, exception);
        }

    }
}
