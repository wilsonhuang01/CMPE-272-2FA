package com.wilson.cmpe272.controller;

import com.wilson.cmpe272.dto.*;
import com.wilson.cmpe272.service.AuthService;
import com.wilson.cmpe272.entity.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<AuthResponse> initiateLogin(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login initiation request received for email: {}", loginRequest.getEmail());
        try {
            AuthResponse response = authService.initiateLogin(loginRequest.getEmail(), loginRequest.getPassword());
            logger.info("Login initiation successful for email: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login initiation failed for email: {} - Error: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/login-verify")
    public ResponseEntity<AuthResponse> completeLogin(@Valid @RequestBody VerificationRequest verificationRequest) {
        logger.info("Login completion request received for email: {}", verificationRequest.getEmail());
        try {
            AuthResponse response = authService.completeLogin(verificationRequest);
            logger.info("Login completion successful for email: {}", verificationRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login completion failed for email: {} - Error: {}", verificationRequest.getEmail(), e.getMessage());
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

            if (change2FARequest.getNewTwoFactorMethod() == User.TwoFactorMethod.EMAIL) {
                response.setMessage("2FA method change successful to: " + change2FARequest.getNewTwoFactorMethod());
            } else if (change2FARequest.getNewTwoFactorMethod() == User.TwoFactorMethod.AUTHENTICATOR_APP) {
                response.setMessage("Changing 2FA method to authenticator app. Verification required.");
            }
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
    
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        logger.info("Logout request received");
        try {
            AuthResponse response = authService.logout();
            logger.info("Logout successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Logout failed - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @PostMapping("/verify-authenticator")
    public ResponseEntity<AuthResponse> verifyAuthenticatorCode(@Valid @RequestBody VerificationRequest verificationRequest) {
        logger.info("Authenticator code verification request received for email: {}", verificationRequest.getEmail());
        try {
            AuthResponse response = authService.verifyAuthenticatorCode(verificationRequest);
            logger.info("Authenticator code verification successful for email: {}", verificationRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Authenticator code verification failed for email: {} - Error: {}", verificationRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getCurrentUserProfile() {
        logger.info("Get current user profile request received");
        try {
            AuthResponse response = authService.getCurrentUserProfile();
            logger.info("User profile retrieved successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Get user profile failed - Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AuthResponse(e.getMessage()));
        }
    }
}
