package com.wilson.cmpe272.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    public String generateToken(UserDetails userDetails) {
        logger.debug("Generating JWT token for user: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDetails.getUsername());
        logger.info("JWT token generated successfully for user: {}", userDetails.getUsername());
        return token;
    }
    
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        logger.debug("Generating JWT token with extra claims for user: {}", userDetails.getUsername());
        String token = createToken(extraClaims, userDetails.getUsername());
        logger.info("JWT token with extra claims generated successfully for user: {}", userDetails.getUsername());
        return token;
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        logger.debug("Creating JWT token for subject: {}", subject);
        String token = Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
        logger.debug("JWT token created successfully for subject: {}", subject);
        return token;
    }
    
    public String extractUsername(String token) {
        logger.debug("Extracting username from JWT token");
        String username = extractClaim(token, Claims::getSubject);
        logger.debug("Username extracted from token: {}", username);
        return username;
    }
    
    public Date extractExpiration(String token) {
        logger.debug("Extracting expiration date from JWT token");
        Date expiration = extractClaim(token, Claims::getExpiration);
        logger.debug("Token expiration date: {}", expiration);
        return expiration;
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        logger.debug("Extracting claim from JWT token");
        final Claims claims = extractAllClaims(token);
        T result = claimsResolver.apply(claims);
        logger.debug("Claim extracted successfully");
        return result;
    }
    
    private Claims extractAllClaims(String token) {
        logger.debug("Extracting all claims from JWT token");
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            logger.debug("All claims extracted successfully");
            return claims;
        } catch (JwtException e) {
            logger.error("Failed to extract claims from JWT token: {}", e.getMessage());
            throw e;
        }
    }
    
    private Boolean isTokenExpired(String token) {
        logger.debug("Checking if JWT token is expired");
        Boolean expired = extractExpiration(token).before(new Date());
        logger.debug("Token expired: {}", expired);
        return expired;
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        logger.debug("Validating JWT token for user: {}", userDetails.getUsername());
        
        // Check if token is blacklisted first
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            logger.warn("JWT token is blacklisted for user: {}", userDetails.getUsername());
            return false;
        }
        
        final String username = extractUsername(token);
        Boolean isValid = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        logger.info("JWT token validation result for user {}: {}", userDetails.getUsername(), isValid);
        return isValid;
    }
    
    public Boolean validateToken(String token) {
        logger.debug("Validating JWT token format and signature");
        
        // Check if token is blacklisted first
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            logger.warn("JWT token is blacklisted");
            return false;
        }
        
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            logger.debug("JWT token format and signature validation successful");
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
