package com.ecommerce_user_authentication.repository;

import com.ecommerce_user_authentication.model.RoleEntity;
import com.ecommerce_user_authentication.model.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Set<RoleEntity> findAllByIdIn(List<Long> roleIds);
    Optional<RoleEntity> findByName(RoleEnum name);
    Boolean existsByName(RoleEnum name);
    Set<RoleEntity> findByNameIn(Set<RoleEnum> selectedRoles);
}
