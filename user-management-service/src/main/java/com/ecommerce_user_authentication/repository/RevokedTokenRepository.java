package com.ecommerce_user_authentication.repository;

import com.ecommerce_user_authentication.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    Optional<RevokedToken> findByJti(String jti);

    @Transactional
    void deleteByExpiryTimeBefore(Date now);
}
