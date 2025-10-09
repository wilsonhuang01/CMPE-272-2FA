package com.wilson.cmpe272.controller;

import com.wilson.cmpe272.dto.*;
import com.wilson.cmpe272.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        logger.info("Signup request received for email: {}", signupRequest.getEmail());
        try {
            AuthResponse response = authService.signup(signupRequest);
            logger.info("Signup successful for email: {}", signupRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Signup failed for email: {} - Error: {}", signupRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login request received for email: {}", loginRequest.getEmail());
        try {
            AuthResponse response = authService.login(loginRequest);
            if (response.getRequiresTwoFactor()) {
                logger.info("Login requires 2FA for email: {}, method: {}", loginRequest.getEmail(), response.getTwoFactorMethod());
            } else {
                logger.info("Login successful for email: {}", loginRequest.getEmail());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for email: {} - Error: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@Valid @RequestBody VerificationRequest verificationRequest) {
        logger.info("Email verification request received for email: {}", verificationRequest.getEmail());
        try {
            AuthResponse response = authService.verifyEmail(verificationRequest);
            logger.info("Email verification successful for email: {}", verificationRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Email verification failed for email: {} - Error: {}", verificationRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/verify-phone")
    public ResponseEntity<AuthResponse> verifyPhone(@Valid @RequestBody VerificationRequest verificationRequest) {
        logger.info("Phone verification request received for email: {}", verificationRequest.getEmail());
        try {
            AuthResponse response = authService.verifyPhone(verificationRequest);
            logger.info("Phone verification successful for email: {}", verificationRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Phone verification failed for email: {} - Error: {}", verificationRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        logger.info("Password change request received");
        try {
            AuthResponse response = authService.changePassword(changePasswordRequest);
            logger.info("Password change successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Password change failed - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/change-2fa")
    public ResponseEntity<AuthResponse> changeTwoFactorMethod(@Valid @RequestBody Change2FARequest change2FARequest) {
        logger.info("2FA method change request received, new method: {}", change2FARequest.getNewTwoFactorMethod());
        try {
            AuthResponse response = authService.changeTwoFactorMethod(change2FARequest);
            logger.info("2FA method change successful to: {}", change2FARequest.getNewTwoFactorMethod());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("2FA method change failed - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/authenticator-qr")
    public ResponseEntity<AuthResponse> getAuthenticatorQrCode() {
        logger.info("Authenticator QR code request received");
        try {
            AuthResponse response = authService.getAuthenticatorQrCode();
            logger.info("Authenticator QR code generated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Authenticator QR code generation failed - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/resend-code")
    public ResponseEntity<AuthResponse> resendVerificationCode(
            @RequestParam String email, 
            @RequestParam String type) {
        logger.info("Resend verification code request received for email: {}, type: {}", email, type);
        try {
            AuthResponse response = authService.resendVerificationCode(email, type);
            logger.info("Verification code resent successfully for email: {}, type: {}", email, type);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Resend verification code failed for email: {}, type: {} - Error: {}", email, type, e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
}
