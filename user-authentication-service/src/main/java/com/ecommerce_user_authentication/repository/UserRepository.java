package com.ecommerce_user_authentication.repository;

import com.ecommerce_user_authentication.model.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findOneByEmail(String email);
}
