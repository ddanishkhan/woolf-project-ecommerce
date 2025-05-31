package com.ecommerce_user_authentication.repository;

import com.ecommerce_user_authentication.model.User;
import com.ecommerce_user_authentication.model.UserActiveToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface UserActiveTokenRepository extends JpaRepository<UserActiveToken, String> {

    List<UserActiveToken> findByUser(User user);

    @Transactional
    void deleteByUser(User user);

    @Transactional
    void deleteByExpiryTimeBefore(Date now);
}
