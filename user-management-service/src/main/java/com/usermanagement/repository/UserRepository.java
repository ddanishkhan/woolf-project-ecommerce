package com.usermanagement.repository;

import com.usermanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByProviderId(String providerId); // To find users by their OAuth provider ID

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}

