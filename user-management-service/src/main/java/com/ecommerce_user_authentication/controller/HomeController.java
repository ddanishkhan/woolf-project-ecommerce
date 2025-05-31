package com.ecommerce_user_authentication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Links to initiate OAuth2 login
        String googleLogin = "<p><a href=\"/oauth2/authorization/google\">Login with Google</a></p>";
        // String facebookLogin = "<p><a href=\"/oauth2/authorization/facebook\">Login with Facebook</a></p>";
        return "<h1>Welcome!</h1>" +
                "<p>Spring Boot OAuth2 login with JWT generation.</p>" +
                googleLogin; // + facebookLogin;
    }

}
