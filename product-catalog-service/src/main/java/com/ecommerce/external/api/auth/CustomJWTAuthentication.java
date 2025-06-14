package com.ecommerce.external.api.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomJWTAuthentication implements Authentication {

    private final String token;
    private final String subject;
    private final String username;
    private final Collection<? extends GrantedAuthority> authorities;
    private boolean authenticated = true;

    public CustomJWTAuthentication(String token, String subject, String username, Collection<? extends GrantedAuthority> authorities) {
        this.token = token;
        this.subject = subject;
        this.username = username;
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.subject;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return username;
    }
}
