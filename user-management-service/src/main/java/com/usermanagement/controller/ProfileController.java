package com.usermanagement.controller;

import com.usermanagement.dto.UpdateProfileRequest;
import com.usermanagement.dto.response.CustomPageDTO;
import com.usermanagement.dto.response.ProfileResponse;
import com.usermanagement.security.JwtTokenProvider;
import com.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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
        String token = jwtTokenProvider.extractAndValidateToken(authorizationHeader);
        String email = jwtTokenProvider.getEmailFromToken(token);
        ProfileResponse profile = userService.getUserProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/all")
    public ResponseEntity<CustomPageDTO<ProfileResponse>> getAllProfiles(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            PageRequest pageRequest) {
        log.info("Get all profiles");
        String token = jwtTokenProvider.extractAndValidateToken(authorizationHeader);
        if (!jwtTokenProvider.isAdmin(token)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        Page<ProfileResponse> profiles = userService.getAllProfiles(pageRequest);
        var response = new CustomPageDTO<>(profiles.getContent(), profiles);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{email}")
    public ResponseEntity<ProfileResponse> getProfileByEmail(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String email) {
        log.info("Get profile data for: {}", email);
        String token = jwtTokenProvider.extractAndValidateToken(authorizationHeader);
        if (!jwtTokenProvider.isAdmin(token)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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

        String token = jwtTokenProvider.extractAndValidateToken(authorizationHeader);
        String email = jwtTokenProvider.getEmailFromToken(token);
        ProfileResponse updatedProfile = userService.updateUserProfileByEmail(email, updateProfileRequest);
        return ResponseEntity.ok(updatedProfile);
    }

}
