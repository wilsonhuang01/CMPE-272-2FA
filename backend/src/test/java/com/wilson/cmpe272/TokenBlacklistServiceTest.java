package com.wilson.cmpe272;

import com.wilson.cmpe272.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TokenBlacklistServiceTest {

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Test
    public void testTokenBlacklisting() {
        String testToken = "test-jwt-token-123";
        
        // Initially, token should not be blacklisted
        assertFalse(tokenBlacklistService.isTokenBlacklisted(testToken));
        
        // Add token to blacklist
        tokenBlacklistService.blacklistToken(testToken);
        
        // Now token should be blacklisted
        assertTrue(tokenBlacklistService.isTokenBlacklisted(testToken));
        
        // Test with null token
        assertFalse(tokenBlacklistService.isTokenBlacklisted(null));
        
        // Test with empty token
        assertFalse(tokenBlacklistService.isTokenBlacklisted(""));
    }
    
    @Test
    public void testRemoveFromBlacklist() {
        String testToken = "test-jwt-token-456";
        
        // Add token to blacklist
        tokenBlacklistService.blacklistToken(testToken);
        assertTrue(tokenBlacklistService.isTokenBlacklisted(testToken));
        
        // Remove from blacklist
        tokenBlacklistService.removeFromBlacklist(testToken);
        assertFalse(tokenBlacklistService.isTokenBlacklisted(testToken));
    }
    
    @Test
    public void testBlacklistSize() {
        int initialSize = tokenBlacklistService.getBlacklistSize();
        
        // Add some tokens
        tokenBlacklistService.blacklistToken("token1");
        tokenBlacklistService.blacklistToken("token2");
        
        assertEquals(initialSize + 2, tokenBlacklistService.getBlacklistSize());
    }
    
    @Test
    public void testClearBlacklist() {
        // Add some tokens
        tokenBlacklistService.blacklistToken("token1");
        tokenBlacklistService.blacklistToken("token2");
        
        assertTrue(tokenBlacklistService.getBlacklistSize() > 0);
        
        // Clear blacklist
        tokenBlacklistService.clearBlacklist();
        assertEquals(0, tokenBlacklistService.getBlacklistSize());
    }
}
