package com.ecommerce_user_authentication.controller;

import com.ecommerce_user_authentication.dto.request.SetUserRolesRequest;
import com.ecommerce_user_authentication.dto.response.AuthenticatedUserResponse;
import com.ecommerce_user_authentication.dto.response.UserInfoResponse;
import com.ecommerce_user_authentication.exception.InvalidTokenException;
import com.ecommerce_user_authentication.model.RoleEnum;
import com.ecommerce_user_authentication.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AuthenticatedUserResponse> authenticatedUser() {
        AuthenticatedUserResponse response;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = authentication.getPrincipal();
        if (user instanceof UserDetails userDetails) {
            Set<RoleEnum> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> RoleEnum.valueOf(role.replace("ROLE_", "")))
                    .collect(Collectors.toSet());
            response = new AuthenticatedUserResponse(userDetails.getUsername(), authorities);
            return ResponseEntity.ok(response);
        }
        log.error("Authentication Principal not handled for {}", user);
        throw new InvalidTokenException("Could not validate user.");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserInfoResponse> getUserInformation(@PathVariable("id") Long userId) {
        var userDto = userService.getUserDetails(userId);
        return userDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserInfoResponse> getUserInformationByEmail(@PathVariable String email) {
        var userDto = userService.getUserDetailsByUserEmail(email);
        return userDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserInfoResponse> setUserRoles(@PathVariable("id") Long userId, @RequestBody @Valid SetUserRolesRequest request) {
        request.roles().add(RoleEnum.USER); // Default role USER.
        var userDto = userService.setUserRole(userId, request.roles());
        return ResponseEntity.ok(userDto);
    }

}
