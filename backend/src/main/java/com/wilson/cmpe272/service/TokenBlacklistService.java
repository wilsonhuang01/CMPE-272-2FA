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
            logger.info("Token blacklisted successfully. Current blacklist size: {}", blacklistedTokens.size());
        } else {
            logger.warn("Attempted to blacklist null or empty token");
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.debug("Checking blacklist for null or empty token - returning false");
            return false;
        }
        boolean isBlacklisted = blacklistedTokens.containsKey(token);
        logger.debug("Token blacklist check result: {} for token ending in: {}", 
            isBlacklisted, token.length() > 10 ? "..." + token.substring(token.length() - 10) : token);
        return isBlacklisted;
    }

    public void removeFromBlacklist(String token) {
        if (token != null) {
            boolean removed = blacklistedTokens.remove(token) != null;
            if (removed) {
                logger.info("Token removed from blacklist successfully. Current blacklist size: {}", blacklistedTokens.size());
            } else {
                logger.debug("Token was not found in blacklist during removal attempt");
            }
        } else {
            logger.warn("Attempted to remove null token from blacklist");
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
