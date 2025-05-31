package com.ecommerce_user_authentication.repository;

import com.ecommerce_user_authentication.model.ERole;
import com.ecommerce_user_authentication.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
