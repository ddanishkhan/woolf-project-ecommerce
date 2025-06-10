package com.ecommerce.config;

import com.ecommerce.exception.AuthenticationException;
import com.ecommerce.external.api.auth.CustomJWTAuthentication;
import com.ecommerce.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    public JwtAuthenticationFilter(HandlerExceptionResolver handlerExceptionResolver, JwtService jwtService) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.jwtService = jwtService;
    }

    // Authentication filter not required on permitted urls.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        // Ensure skipping happens **only for GET requests**
        boolean isGetRequest = requestMethod.equals(HttpMethod.GET.name());

        boolean isPermittedGet = isGetRequest && Arrays.stream(SecurityConfig.PERMITTED_GET_URLS).anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
        boolean isPermittedGeneral = Arrays.stream(SecurityConfig.PERMITTED_URLS).anyMatch(pattern -> PATH_MATCHER.match(pattern, requestUri));
        boolean skipFilter = isPermittedGet || isPermittedGeneral;
        log.debug("Skip filter that parses jwt token : {}", skipFilter);
        return skipFilter;
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
                    log.error("Token session invalid.");
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
