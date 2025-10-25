package com.wilson.cmpe272.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    
    // In-memory storage for blacklisted tokens
    // In production, consider using Redis or database
    private final ConcurrentHashMap<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    
    // Cleanup scheduler to remove expired tokens from blacklist
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public TokenBlacklistService() {
        // Schedule cleanup every hour to remove expired tokens from blacklist
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }
    
    public void blacklistToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            blacklistedTokens.put(token, LocalDateTime.now());
            logger.info("Token blacklisted successfully");
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        return blacklistedTokens.containsKey(token);
    }

    public void removeFromBlacklist(String token) {
        if (token != null) {
            blacklistedTokens.remove(token);
            logger.info("Token removed from blacklist");
        }
    }
    
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }

    public void clearBlacklist() {
        blacklistedTokens.clear();
        logger.info("Token blacklist cleared");
    }
    
    private void cleanupExpiredTokens() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24); // Keep tokens for 24 hours
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        logger.debug("Cleaned up expired tokens from blacklist. Current size: {}", blacklistedTokens.size());
    }
}
