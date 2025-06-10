package com.usermanagement.service;

import com.usermanagement.dto.UpdateProfileRequest;
import com.usermanagement.dto.response.ProfileResponse;
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

    @Autowired
    public void setPasswordEncoder(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Lazy private PasswordEncoder passwordEncoder;

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
            String username = email.split("@")[0] + "_" + oauthProviderName.toLowerCase().replaceAll("[^a-zA-Z0-9_]", "");
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
     * @param user The User entity to map.
     * @return The corresponding ProfileResponse DTO.
     */
    private ProfileResponse mapUserToProfileResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId().toString());
        response.setName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setProvider(user.getProvider().toString());
        response.setRoles(user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toSet()));
        return response;
    }

    @Transactional
    public String createPasswordResetTokenForUser(User user) {
        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);
        PasswordResetToken myToken = new PasswordResetToken(user);
        passwordResetTokenRepository.save(myToken);
        return myToken.getToken();
    }

    public Optional<PasswordResetToken> getPasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Transactional
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
