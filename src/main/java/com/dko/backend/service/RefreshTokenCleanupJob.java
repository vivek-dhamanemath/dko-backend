package com.dko.backend.service;

import com.dko.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Cleanup expired and old revoked refresh tokens
     * Runs every hour at the top of the hour
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanRefreshTokens() {
        Instant now = Instant.now();
        Instant revokedThreshold = now.minus(30, ChronoUnit.DAYS);

        // Delete expired tokens
        int expiredDeleted = refreshTokenRepository.deleteExpired(now);

        // Delete revoked tokens older than 30 days
        int revokedDeleted = refreshTokenRepository.deleteOldRevoked(revokedThreshold);

        log.info(
                "RefreshToken cleanup completed: expired={}, old_revoked={}",
                expiredDeleted,
                revokedDeleted);
    }
}
