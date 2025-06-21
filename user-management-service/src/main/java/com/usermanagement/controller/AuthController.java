package com.usermanagement.controller;

import com.usermanagement.dto.ForgotPasswordRequest;
import com.usermanagement.dto.response.JwtAuthenticationResponse;
import com.usermanagement.dto.LoginRequest;
import com.usermanagement.dto.RegisterRequest;
import com.usermanagement.dto.ResetPasswordRequest;
import com.usermanagement.dto.response.MessageResponse;
import com.usermanagement.model.PasswordResetToken;
import com.usermanagement.model.User;
import com.usermanagement.repository.PasswordResetTokenRepository;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.service.TokenBlacklistService;
import com.usermanagement.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordResetTokenRepository passwordResetTokenRepository; //FIXME use a service.

    @GetMapping("/login-page")
    public ModelAndView loginPage() {
        return new ModelAndView("login");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User appUser = userService.findByUsername(userDetails.getUsername()); // Fetch User object

        if (appUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: User details not found.");
        }
        String displayName = appUser.getDisplayName() != null ? appUser.getDisplayName() : appUser.getUsername();

        // Use the JwtTokenProvider method that takes User object
        String jwt = jwtTokenProvider.generateInternalToken(
                appUser, // Pass the User object
                displayName,
                userDetails.getAuthorities()
        );
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {

        userService.registerNewUser(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPassword(),
                registerRequest.getDisplayName()
        );
        return ResponseEntity.ok(new MessageResponse("User registered successfully! Please login."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        if (StringUtils.hasText(jwt)) {

            // To revoke all tokens for the user, we need the User object.
            // We get the username from the token being used for logout.
            String username = jwtTokenProvider.getUsernameFromJWT(jwt);
            User user = userService.findByUsername(username);

            if (user != null) {
                tokenBlacklistService.revokeAllTokensForUser(user);
                // SecurityContextHolder.clearContext(); // Good practice for stateless apps too
                return ResponseEntity.ok(new MessageResponse("User logged out successfully. All active tokens have been revoked."));
            } else {
                // This case should be rare if the token was valid to get username
                // Fallback: blacklist only the current token if user not found (though this indicates an issue)
                String jti = jwtTokenProvider.getJtiFromJWT(jwt);
                java.util.Date expiryDate = jwtTokenProvider.getExpirationDateFromJWT(jwt);
                tokenBlacklistService.addToBlacklist(jti, expiryDate);
                return ResponseEntity.ok(new MessageResponse("Logged out. Current token blacklisted (user context not fully resolved)."));
            }

        }
        return ResponseEntity.badRequest().body("Logout failed: No token provided or token malformed.");
    }

    //TODO: incomplete.
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        User user = userService.findByEmail(forgotPasswordRequest.getEmail());
        if (user == null) {
            return ResponseEntity.ok(new MessageResponse("If an account with this email exists, a password reset link has been sent."));
        }
        String token = userService.createPasswordResetTokenForUser(user);
        String resetLink = "https://yourapp.com/reset-password?token=" + token;
        log.info("Password Reset Link (for user {}): {}", user.getEmail(), resetLink);
        return ResponseEntity.ok(new MessageResponse("If an account with this email exists, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        Optional<PasswordResetToken> tokenOptional = userService.getPasswordResetToken(resetPasswordRequest.getToken());
        if (tokenOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid or missing password reset token."));
        }
        PasswordResetToken resetToken = tokenOptional.get();
        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            return ResponseEntity.badRequest().body(new MessageResponse("Password reset token has expired."));
        }
        if (resetToken.isUsed()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Password reset token has already been used."));
        }
        User user = resetToken.getUser();
        userService.changeUserPassword(user, resetPasswordRequest.getNewPassword()); // This now revokes all tokens
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        return ResponseEntity.ok(new MessageResponse("Password has been successfully reset. All previous sessions have been invalidated."));
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        return TokenValidationController.extractJwtFromRequest(request);
    }
}
