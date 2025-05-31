package com.usermanagement.config;

import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.service.UserDetailsServiceImpl;
import com.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final JwtTokenProvider jwtTokenProvider;
    private final String frontendRedirectUri;
    // UserDetailsServiceImpl is autowired by Spring as it's a @Service. used by the AuthenticationManager.
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final UserService userService;

    @Autowired
    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          @Value("${app.jwt.redirect-uri-after-login}") String frontendRedirectUri,
                          UserDetailsServiceImpl userDetailsServiceImpl,
                          UserService userService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.frontendRedirectUri = frontendRedirectUri;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.userService = userService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler() {
        return new OAuth2LoginSuccessHandler(jwtTokenProvider, frontendRedirectUri, userService);
    }

    /**
     * Exposes the AuthenticationManager as a Bean.
     * Spring Security will construct the AuthenticationManager using the available
     * UserDetailsService (UserDetailsServiceImpl) and PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                // No need for .authenticationManager(...) here if relying on the global bean from AuthenticationConfiguration
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/error", "/login", "/oauth2/**", "/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                                .loginPage("/login")
                                .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                                .successHandler(oAuth2LoginSuccessHandler())
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://frontend-domain.com", "http://localhost:8080", "http://localhost:8005"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}

