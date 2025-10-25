package com.wilson.cmpe272.service;

import com.wilson.cmpe272.dto.*;
import com.wilson.cmpe272.entity.User;
import com.wilson.cmpe272.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private TwoFactorService twoFactorService;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    public AuthResponse signup(SignupRequest signupRequest) {
        logger.info("Starting signup process for email: {}", signupRequest.getEmail());
        
        // Validate password confirmation
        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            logger.warn("Password confirmation mismatch for email: {}", signupRequest.getEmail());
            throw new IllegalArgumentException("Password and confirmation do not match");
        }
        
        // Check if user already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            logger.warn("Signup attempted with existing email: {}", signupRequest.getEmail());
            throw new IllegalArgumentException("User with this email already exists");
        }
        
        // Create new user
        logger.info("Creating new user account for email: {}", signupRequest.getEmail());
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setTwoFactorMethod(signupRequest.getTwoFactorMethod());
        
        // Setup 2FA if requested
        if (signupRequest.getTwoFactorMethod() != null) {
            logger.info("Setting up 2FA for user: {}, method: {}", signupRequest.getEmail(), signupRequest.getTwoFactorMethod());
            if (signupRequest.getTwoFactorMethod() == User.TwoFactorMethod.AUTHENTICATOR_APP) {
                twoFactorService.setupAuthenticatorApp(user);
            }
            user.setIsTwoFactorEnabled(true);
        }

        // Send email verification
        logger.info("Sending email verification code to: {}", signupRequest.getEmail());
        twoFactorService.sendEmailVerificationCode(user);
        
        user = userRepository.save(user);
        logger.info("User account created successfully with ID: {} for email: {}", user.getId(), signupRequest.getEmail());
        
        return new AuthResponse(user, "User created successfully. Please check your email for verification code.");
    }
    
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("Starting login process for email: {}", loginRequest.getEmail());
        try {
            logger.debug("Authenticating user credentials for email: {}", loginRequest.getEmail());
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            
            User user = (User) authentication.getPrincipal();
            logger.info("User authentication successful for email: {}", loginRequest.getEmail());
            
            // Check if 2FA is required
            if (user.getIsTwoFactorEnabled() && user.getTwoFactorMethod() != null) {
                logger.info("2FA is enabled for user: {}, method: {}", loginRequest.getEmail(), user.getTwoFactorMethod());
                if (loginRequest.getTwoFactorCode() == null) {
                    // Send 2FA code
                    logger.info("Sending 2FA code for user: {}", loginRequest.getEmail());
                    twoFactorService.sendTwoFactorCode(user);
                    AuthResponse response = new AuthResponse("Two-factor authentication required");
                    response.setRequiresTwoFactor(true);
                    response.setTwoFactorMethod(user.getTwoFactorMethod());
                    userRepository.save(user);
                    return response;
                } else {
                    // Verify 2FA code
                    logger.debug("Verifying 2FA code for user: {}", loginRequest.getEmail());
                    if (!twoFactorService.verifyTwoFactorCode(user, loginRequest.getTwoFactorCode())) {
                        logger.warn("Invalid 2FA code provided for user: {}", loginRequest.getEmail());
                        throw new BadCredentialsException("Invalid two-factor authentication code");
                    }
                    logger.info("2FA verification successful for user: {}", loginRequest.getEmail());
                }
            }
            
            // Update last login
            logger.debug("Updating last login time for user: {}", loginRequest.getEmail());
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Generate JWT token
            logger.debug("Generating JWT token for user: {}", loginRequest.getEmail());
            String token = jwtService.generateToken(user);
            
            logger.info("Login completed successfully for user: {}", loginRequest.getEmail());
            return new AuthResponse(token, user);
            
        } catch (Exception e) {
            logger.error("Login failed for email: {} - Error: {}", loginRequest.getEmail(), e.getMessage());
            throw new BadCredentialsException("Invalid email or password");
        }
    }
    
    public AuthResponse verifyEmail(VerificationRequest verificationRequest) {
        logger.info("Email verification attempt for email: {}", verificationRequest.getEmail());
        User user = userRepository.findByEmail(verificationRequest.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Email verification failed - user not found: {}", verificationRequest.getEmail());
                    return new IllegalArgumentException("User not found");
                });
        
        if (twoFactorService.verifyEmailCode(user, verificationRequest.getCode())) {
            logger.info("Email verification successful for user: {}", verificationRequest.getEmail());
            user.setIsEmailVerified(true);
            user.setEmailVerificationCode(null);
            user.setEmailVerificationExpiresAt(null);
            user.setIsTwoFactorEnabled(true);
            user.setTwoFactorMethod(User.TwoFactorMethod.EMAIL);
            userRepository.save(user);
            
            return new AuthResponse("Email verified successfully");
        } else {
            logger.warn("Email verification failed - invalid or expired code for user: {}", verificationRequest.getEmail());
            throw new IllegalArgumentException("Invalid or expired verification code");
        }
    }
    
    
    public AuthResponse changePassword(ChangePasswordRequest changePasswordRequest) {
        User user = getCurrentUser();
        logger.info("Password change request for user: {}", user.getEmail());
        
        // Verify current password
        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            logger.warn("Password change failed - incorrect current password for user: {}", user.getEmail());
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password confirmation
        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())) {
            logger.warn("Password change failed - password confirmation mismatch for user: {}", user.getEmail());
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
        
        // Update password
        logger.info("Updating password for user: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", user.getEmail());
        
        return new AuthResponse("Password changed successfully");
    }
    
    public AuthResponse changeTwoFactorMethod(Change2FARequest change2FARequest) {
        User user = getCurrentUser();
        logger.info("2FA method change request for user: {} to method: {}", user.getEmail(), change2FARequest.getNewTwoFactorMethod());
        
        // Verify password
        if (!passwordEncoder.matches(change2FARequest.getPassword(), user.getPassword())) {
            logger.warn("2FA method change failed - incorrect password for user: {}", user.getEmail());
            throw new IllegalArgumentException("Password is incorrect");
        }
        
        // Setup new 2FA method
        logger.info("Setting up new 2FA method: {} for user: {}", change2FARequest.getNewTwoFactorMethod(), user.getEmail());
        user.setTwoFactorMethod(change2FARequest.getNewTwoFactorMethod());
        user.setIsTwoFactorEnabled(true);
        
        if (change2FARequest.getNewTwoFactorMethod() == User.TwoFactorMethod.AUTHENTICATOR_APP) {
            logger.info("Setting up authenticator app for user: {}", user.getEmail());
            twoFactorService.setupAuthenticatorApp(user);
        }
        
        userRepository.save(user);
        logger.info("2FA method changed successfully for user: {}", user.getEmail());
        
        if (change2FARequest.getNewTwoFactorMethod() == User.TwoFactorMethod.AUTHENTICATOR_APP) {
            AuthResponse response = new AuthResponse("Authenticator app setup initiated");
            response.setMessage("Please scan the QR code with your authenticator app");
            return response;
        }
        
        return new AuthResponse("Two-factor authentication method changed. Please verify your new method.");
    }
    
    public AuthResponse getAuthenticatorQrCode() {
        User user = getCurrentUser();
        logger.info("QR code request for authenticator app from user: {}", user.getEmail());
        
        if (user.getTwoFactorMethod() != User.TwoFactorMethod.AUTHENTICATOR_APP) {
            logger.warn("QR code request failed - authenticator app not enabled for user: {}", user.getEmail());
            throw new IllegalArgumentException("Authenticator app is not enabled for this user");
        }
        
        String qrCodeUrl = twoFactorService.getAuthenticatorAppQrCode(user);
        logger.info("QR code generated successfully for user: {}", user.getEmail());
        AuthResponse response = new AuthResponse("QR Code generated");
        response.setMessage(qrCodeUrl);
        return response;
    }
    
    public AuthResponse resendVerificationCode(String email, String type) {
        logger.info("Resend verification code request for email: {}, type: {}", email, type);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Resend verification code failed - user not found: {}", email);
                    return new IllegalArgumentException("User not found");
                });
        
        if ("email".equals(type)) {
            logger.info("Resending email verification code to: {}", email);
            twoFactorService.sendEmailVerificationCode(user);
            userRepository.save(user);
            return new AuthResponse("Email verification code sent");
        }
        
        logger.warn("Invalid verification type requested: {} for email: {}", type, email);
        throw new IllegalArgumentException("Invalid verification type. Only 'email' is supported.");
    }
    
    public AuthResponse logout() {
        logger.info("Processing logout request");
        try {
            // Extract JWT token from request
            String token = extractTokenFromRequest();
            if (token != null) {
                // Add token to blacklist
                tokenBlacklistService.blacklistToken(token);
                logger.info("Token added to blacklist during logout");
            }
            
            // Clear the security context
            SecurityContextHolder.clearContext();
            logger.info("Security context cleared successfully");
            return new AuthResponse("Logged out successfully");
        } catch (Exception e) {
            logger.error("Logout failed - Error: {}", e.getMessage());
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }
    
    /**
     * Extract JWT token from the Authorization header
     * @return The JWT token or null if not found
     */
    private String extractTokenFromRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7); // Remove "Bearer " prefix
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract token from request: {}", e.getMessage());
        }
        return null;
    }
    
    public AuthResponse getCurrentUserProfile() {
        logger.info("Retrieving current user profile");
        try {
            User user = getCurrentUser();
            logger.info("User profile retrieved for email: {}", user.getEmail());
            
            // Create a response with user profile information
            AuthResponse response = new AuthResponse("User profile retrieved successfully");
            response.setUser(user);
            return response;
        } catch (Exception e) {
            logger.error("Get user profile failed - Error: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve user profile: " + e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            logger.error("No authentication found in SecurityContext");
            throw new RuntimeException("User not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        logger.debug("Retrieved current user: {}, Status: {}, IsEmailVerified: {}, IsEnabled: {}", 
            user.getEmail(), user.getStatus(), user.getIsEmailVerified(), user.isEnabled());
        return user;
    }
}
