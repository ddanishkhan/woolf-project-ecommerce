package com.ecommerce_user_authentication.service;

import com.ecommerce_user_authentication.dto.response.UserInfoResponse;
import com.ecommerce_user_authentication.exception.InvalidRoleException;
import com.ecommerce_user_authentication.exception.UserNotFoundException;
import com.ecommerce_user_authentication.model.RoleEntity;
import com.ecommerce_user_authentication.model.RoleEnum;
import com.ecommerce_user_authentication.model.UserEntity;
import com.ecommerce_user_authentication.repository.RoleRepository;
import com.ecommerce_user_authentication.repository.SessionRepository;
import com.ecommerce_user_authentication.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SessionRepository sessionRepository;

    public Optional<UserInfoResponse> getUserDetails(Long userId) {
        var userEntity = userRepository.findById(userId);
        return userEntity.map(UserInfoResponse::from);
    }

    public Optional<UserInfoResponse> getUserDetailsByUserEmail(String email) {
        var userEntity = userRepository.findOneByEmail(email);
        return userEntity.map(UserInfoResponse::from);
    }

    @Transactional
    public UserInfoResponse setUserRole(Long userId, Set<RoleEnum> roles) {

        // User cannot update his own role.
        var currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        var currentUser = userRepository.findOneByEmail(currentUserEmail).orElseThrow();
        if (userId.equals(currentUser.getId())) {
            throw new InvalidRoleException("You cannot update your own role.");
        }

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        Set<RoleEntity> rolesEntity = validatedRoles(roles);
        user.setRoleEntity(rolesEntity);
        userRepository.save(user);
        UserEntity updatedUser = userRepository.findById(userId).orElseThrow();

        // Invalidate the existing tokens for the updated user.
        sessionRepository.deactivateUpdatedUser(userId);

        return UserInfoResponse.from(updatedUser);
    }

    public Set<RoleEntity> validatedRoles(Set<RoleEnum> selectedRoles) {
        // Fetch all roles from the database that match the selected roles
        Set<RoleEntity> roleEntities = roleRepository.findByNameIn(selectedRoles);

        // Extract role names from the fetched RoleEntity objects
        Set<RoleEnum> foundRoles = roleEntities.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());

        // Check if any of the selected roles are missing
        selectedRoles.removeAll(foundRoles);
        if (!selectedRoles.isEmpty()) {
            throw new InvalidRoleException("Roles not found: " + selectedRoles);
        }

        return roleEntities;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userOpt = userRepository.findOneByEmail(username);
        if (userOpt.isPresent()){
            UserEntity user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), getAuthority(user));
        }
        throw new UsernameNotFoundException("Invalid username or password.");
    }

    private Set<SimpleGrantedAuthority> getAuthority(UserEntity user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoleEntity().forEach(
                role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName())));
        return authorities;
    }

}
