package com.ecommerce_user_authentication.controller;

import com.ecommerce_user_authentication.dto.request.ChangePasswordRequest;
import com.ecommerce_user_authentication.dto.request.LoginRequest;
import com.ecommerce_user_authentication.dto.request.SignUpRequest;
import com.ecommerce_user_authentication.dto.request.ValidateTokenRequest;
import com.ecommerce_user_authentication.dto.response.AuthToken;
import com.ecommerce_user_authentication.dto.response.SessionStatusResponse;
import com.ecommerce_user_authentication.dto.response.UserInfoResponse;
import com.ecommerce_user_authentication.service.AuthenticationService;
import com.ecommerce_user_authentication.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, JwtService jwtService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthToken> login(@RequestBody @Valid LoginRequest request) {
        final String token = authenticationService.login(request.email(), request.password());
        return ResponseEntity.ok(new AuthToken(token));
    }

    @PostMapping("/logout/{email}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@PathVariable String email, @RequestHeader("token") String token) {
        authenticationService.logout(token, email);
        return ResponseEntity.status(HttpStatus.RESET_CONTENT).build();
    }

    @PostMapping("/signup")
    public ResponseEntity<UserInfoResponse> signUp(@RequestBody @Valid SignUpRequest request) {
        Optional<UserInfoResponse> userDto = authenticationService.signUp(request.email(), request.password());
        return userDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authenticationService.changePassword(request.email(), request.password(), request.newPassword());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/validate")
    public ResponseEntity<SessionStatusResponse> validateToken(@RequestBody @Valid ValidateTokenRequest request) {
        var sessionStatus = authenticationService.validate(request.token(), request.email());
        return ResponseEntity.ok(new SessionStatusResponse(sessionStatus.name()));
    }

}
