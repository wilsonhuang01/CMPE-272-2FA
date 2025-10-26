package com.wilson.cmpe272.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;
        
        logger.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No valid Authorization header found, proceeding without authentication");
            filterChain.doFilter(request, response);
            return;
        }
        
        jwt = authHeader.substring(7);
        logger.debug("Extracting JWT token from Authorization header");
        
        try {
            userEmail = jwtService.extractUsername(jwt);
            logger.debug("Extracted username from JWT: {}", userEmail);
        } catch (Exception e) {
            logger.warn("Failed to extract username from JWT token: {}", e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }
        
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Loading user details for authentication: {}", userEmail);
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtService.validateToken(jwt, userDetails)) {
                    logger.info("JWT token validated successfully for user: {}. User enabled: {}. Account non-locked: {}",
                        userEmail, userDetails.isEnabled(), userDetails.isAccountNonLocked());
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication context set for user: {}", userEmail);
                } else {
                    logger.warn("JWT token validation failed for user: {}", userEmail);
                }
            } catch (Exception e) {
                logger.error("Error during JWT authentication for user: {} - Error: {}", userEmail, e.getMessage());
            }
        } else if (userEmail == null) {
            logger.debug("No username extracted from JWT token");
        } else {
            logger.debug("User already authenticated, skipping JWT validation");
        }
        
        filterChain.doFilter(request, response);
    }
}
