package com.ecommerce_user_authentication.service;

import com.ecommerce_user_authentication.model.RevokedToken;
import com.ecommerce_user_authentication.model.User;
import com.ecommerce_user_authentication.model.UserActiveToken;
import com.ecommerce_user_authentication.repository.RevokedTokenRepository;
import com.ecommerce_user_authentication.repository.UserActiveTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final UserActiveTokenRepository userActiveTokenRepository;

    /**
     * Adds a specific token's JTI to the blacklist (database).
     * This might be used for admin-initiated single token revocation.
     *
     * @param jti The JWT ID (JTI claim) of the token to blacklist.
     * @param expiryTime The original expiry time of the token.
     */
    @Transactional
    public void addToBlacklist(String jti, Date expiryTime) {
        if (jti != null && expiryTime != null && revokedTokenRepository.findByJti(jti).isEmpty()) {
                RevokedToken revokedToken = new RevokedToken(jti, expiryTime);
                revokedTokenRepository.save(revokedToken);
                // Also remove from active tokens if it was there
                userActiveTokenRepository.deleteById(jti);
            }
    }

    /**
     * Revokes all active tokens for a given user.
     * Moves their JTIs from UserActiveToken to RevokedToken.
     *
     * @param user The user whose tokens are to be revoked.
     */
    @Transactional
    public void revokeAllTokensForUser(User user) {
        if (user == null) return;

        List<UserActiveToken> activeTokens = userActiveTokenRepository.findByUser(user);
        for (UserActiveToken activeToken : activeTokens) {
            if (revokedTokenRepository.findByJti(activeToken.getJti()).isEmpty()) {
                RevokedToken revoked = new RevokedToken(activeToken.getJti(), activeToken.getExpiryTime());
                revokedTokenRepository.save(revoked);
            }
        }
        // Delete all active tokens for the user after moving them to blacklist
        userActiveTokenRepository.deleteByUser(user);
        log.info("Revoked all active tokens for user: {}", user.getUsername());
    }


    public boolean isBlacklisted(String jti) {
        if (jti == null) {
            return false;
        }
        return revokedTokenRepository.findByJti(jti).isPresent();
    }

    /**
     * Scheduled cleanup for RevokedToken (blacklist) table.
     */
    @Scheduled(cron = "0 0 1 * * *") // Daily at 1 AM
    @Transactional
    public void cleanupExpiredRevokedTokens() {
        Date now = new Date();
        revokedTokenRepository.deleteByExpiryTimeBefore(now);
        log.info("Cleaned up expired JTIs from database blacklist (RevokedToken table).");
    }

    /**
     * Scheduled cleanup for UserActiveToken table to remove naturally expired tokens.
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    @Transactional
    public void cleanupExpiredActiveTokens() {
        Date now = new Date();
        userActiveTokenRepository.deleteByExpiryTimeBefore(now);
        log.info("Cleaned up naturally expired JTIs from UserActiveToken table.");
    }
}
