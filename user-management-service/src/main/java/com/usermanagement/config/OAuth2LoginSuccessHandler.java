package com.usermanagement.config; // Ensure package is correct

import com.usermanagement.model.User;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final String frontendRedirectUri;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User oauth2User = oauthToken.getPrincipal();
            String registrationId = oauthToken.getAuthorizedClientRegistrationId();

            Map<String, Object> attributes = oauth2User.getAttributes();
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String providerId = oauth2User.getName();

            User localUser = userService.processOAuth2User(providerId, email, name, registrationId);

//            String jwtSubject = localUser.getUsername();
            String displayName = localUser.getDisplayName();

            // Get authorities from the localUser's roles
            Collection<? extends GrantedAuthority> authorities = localUser.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                    .toList();

            String jwt = jwtTokenProvider.generateInternalToken(localUser, displayName, authorities);

            String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                    .queryParam("token", jwt)
                    .queryParam("userId", localUser.getId())
                    .queryParam("displayName", displayName)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
