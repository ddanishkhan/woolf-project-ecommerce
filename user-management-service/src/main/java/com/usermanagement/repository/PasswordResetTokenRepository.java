package com.usermanagement.repository;

import com.usermanagement.model.PasswordResetToken;
import com.usermanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUser(User user);

    @Transactional
    void deleteByExpiryDateBefore(Date now);

    @Transactional
    void deleteByUserAndTokenNot(User user, String currentToken); // To delete old tokens for a user
}
