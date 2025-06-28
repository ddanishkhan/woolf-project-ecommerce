package com.usermanagement.service;

import com.usermanagement.dto.ResetPasswordRequest;
import com.usermanagement.dto.UpdateProfileRequest;
import com.usermanagement.dto.response.MessageResponse;
import com.usermanagement.dto.response.ProfileResponse;
import com.usermanagement.events.dto.PasswordResetTokenEvent;
import com.usermanagement.events.publisher.PasswordResetTokenEventPublisher;
import com.usermanagement.exception.UserAlreadyExistsException;
import com.usermanagement.exception.UserNotFoundException;
import com.usermanagement.model.AuthProvider;
import com.usermanagement.model.ERole;
import com.usermanagement.model.PasswordResetToken;
import com.usermanagement.model.Role;
import com.usermanagement.model.User;
import com.usermanagement.repository.PasswordResetTokenRepository;
import com.usermanagement.repository.RoleRepository;
import com.usermanagement.repository.UserActiveTokenRepository;
import com.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserActiveTokenRepository userActiveTokenRepository;
    private final PasswordResetTokenEventPublisher resetTokenEventPublisher;

    @Autowired
    public void setPasswordEncoder(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Lazy
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User processOAuth2User(String providerId, String email, String name, String oauthProviderName) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() == AuthProvider.LOCAL) {
                log.info("User with email {} already exists with LOCAL provider. Linking OAuth account.", email);
            }
            if (user.getProviderId() == null || !user.getProviderId().equals(providerId)) {
                user.setProviderId(providerId);
            }
            user.setProvider(AuthProvider.valueOf(oauthProviderName.toUpperCase()));
            user.setDisplayName(name);
        } else {
            user = new User();
            user.setEmail(email);
            user.setDisplayName(name);
            user.setProvider(AuthProvider.valueOf(oauthProviderName.toUpperCase()));
            user.setProviderId(providerId);
            String username = email.split("@")[0] + "_" + oauthProviderName.toLowerCase().replaceAll("\\W", "");
            if (Boolean.TRUE.equals(userRepository.existsByUsername(username))) {
                username = username + "_" + UUID.randomUUID().toString().substring(0, 4);
            }
            user.setUsername(username);
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER is not found."));
            roles.add(userRole);
            user.setRoles(roles);
        }
        return userRepository.save(user);
    }

    @Transactional
    public User registerNewUser(String username, String email, String password, String displayName) {
        if (Boolean.TRUE.equals(userRepository.existsByUsername(username))) {
            throw new UserAlreadyExistsException("Username already exists: " + username);
        }
        if (Boolean.TRUE.equals(userRepository.existsByEmail(email))) {
            throw new UserAlreadyExistsException("Email already exists: " + email);
        }
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setDisplayName(displayName);
        newUser.setProvider(AuthProvider.LOCAL);
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.USER)
                .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER is not found. Make sure it's initialized."));
        roles.add(userRole);
        newUser.setRoles(roles);
        return userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public ProfileResponse getUserProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        return mapUserToProfileResponse(user);
    }

    public ProfileResponse updateUserProfileByEmail(String email, UpdateProfileRequest updateProfileRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        user.setDisplayName(updateProfileRequest.getName());
        User updatedUser = userRepository.save(user);
        return mapUserToProfileResponse(updatedUser);
    }

    /**
     * Helper method to map a User entity to a ProfileResponse DTO.
     *
     * @param user The User entity to map.
     * @return The corresponding ProfileResponse DTO.
     */
    private ProfileResponse mapUserToProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId().toString());
        response.setName(user.getDisplayName());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setProvider(user.getProvider().toString());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toSet()));
        return response;
    }

    @Transactional
    public ResponseEntity<MessageResponse> createPasswordResetTokenForUser(User user) {
        PasswordResetToken myToken = passwordResetTokenRepository.findByUser(user)
                .orElse(new PasswordResetToken(user)); // If no token, create a new one
        passwordResetTokenRepository.save(myToken);
        String token = myToken.getToken();
        String resetLink = "http://localhost:8005/reset-password?token=" + token;
        log.info("Password Reset Link (for user {}) Generated, expires at: {}", user.getEmail(), myToken.getExpiryDate());
        //send to notification service to send email.
        resetTokenEventPublisher.publishEvent(new PasswordResetTokenEvent(user.getEmail(), resetLink, myToken.getExpiryDate()));
        return ResponseEntity.ok(new MessageResponse("If an account with this email exists, a password reset link has been sent."));
    }


    @Transactional
    public ResponseEntity<MessageResponse> resetPassword(ResetPasswordRequest resetPasswordRequest){
        // move some parts to controller?
        Optional<PasswordResetToken> tokenOptional = getPasswordResetToken(resetPasswordRequest.getToken());
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
        changeUserPassword(user, resetPasswordRequest.getNewPassword());
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        return ResponseEntity.ok(new MessageResponse("Password has been successfully reset. All previous sessions have been invalidated."));
    }

    public Optional<PasswordResetToken> getPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    /**
     * Changes the password and revokes all tokens.
     */
    public void changeUserPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalidate all active tokens for this user
        tokenBlacklistService.revokeAllTokensForUser(user);
        log.info("Password for user {} changed. All active JWTs have been revoked.", user.getUsername());
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void purgeExpiredPasswordResetTokens() {
        passwordResetTokenRepository.deleteByExpiryDateBefore(new Date());
        log.info("Purged expired password reset tokens.");
    }

}
