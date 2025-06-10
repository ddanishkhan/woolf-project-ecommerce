package com.usermanagement.controller;

import com.usermanagement.dto.UpdateProfileRequest;
import com.usermanagement.dto.response.ProfileResponse;
import com.usermanagement.exception.InvalidTokenException;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Retrieves the profile of the user identified by the provided JWT.
     * This endpoint manually validates the token from the Authorization header.
     *
     * @param authorizationHeader The 'Authorization' header containing the Bearer token.
     * @return A ResponseEntity containing the user's profile information.
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getUserProfileManually(@Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractAndValidateToken(authorizationHeader);
        String email = jwtTokenProvider.getEmailFromToken(token);
        ProfileResponse profile = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    /**
     * Updates the profile of the currently authenticated user.
     * This endpoint requires the user to be authenticated.
     *
     * @param updateProfileRequest The request body containing the updated details.
     * @return A ResponseEntity containing the updated user's profile information.
     */
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateUserProfile(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody UpdateProfileRequest updateProfileRequest) {

        String token = extractAndValidateToken(authorizationHeader);
        String email = jwtTokenProvider.getEmailFromToken(token);
        ProfileResponse updatedProfile = userService.updateUserProfileByEmail(email, updateProfileRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Helper method to extract the JWT from the header and validate it.
     *
     * @param authorizationHeader The full Authorization header value.
     * @return The raw, validated token string.
     * @throws InvalidTokenException if the token is missing or invalid.
     */
    private String extractAndValidateToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Authorization header is missing or invalid.");
        }
        String token = authorizationHeader.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new InvalidTokenException("Invalid or expired JWT token.");
        }
        return token;
    }

}
