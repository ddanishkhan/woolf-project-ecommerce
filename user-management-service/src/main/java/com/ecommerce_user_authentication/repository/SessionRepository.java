package com.ecommerce_user_authentication.repository;

import com.ecommerce_user_authentication.model.SessionEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionRepository extends CrudRepository<SessionEntity, Long> {

    Optional<SessionEntity> findByTokenAndUser_Email(String token, String email);

    @Modifying
    @Query("UPDATE SessionEntity s SET s.sessionStatus = 'INVALID' WHERE id = :user_id")
    void deactivateUpdatedUser(@Param("user_id") Long userId);

}
