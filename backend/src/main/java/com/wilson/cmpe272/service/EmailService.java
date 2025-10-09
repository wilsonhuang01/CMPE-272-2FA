package com.wilson.cmpe272.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendVerificationCode(String toEmail, String verificationCode) {
        logger.info("Sending email verification code to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification Code");
            message.setText("Your verification code is: " + verificationCode + 
                           "\n\nThis code will expire in 10 minutes." +
                           "\n\nIf you didn't request this code, please ignore this email.");
            
            mailSender.send(message);
            logger.info("Email verification code sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send email verification code to: {} - Error: {}", toEmail, e.getMessage());
            throw e;
        }
    }
    
    public void sendTwoFactorCode(String toEmail, String verificationCode) {
        logger.info("Sending 2FA code via email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Two-Factor Authentication Code");
            message.setText("Your two-factor authentication code is: " + verificationCode + 
                           "\n\nThis code will expire in 5 minutes." +
                           "\n\nIf you didn't request this code, please contact support immediately.");
            
            mailSender.send(message);
            logger.info("2FA code sent successfully via email to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send 2FA code via email to: {} - Error: {}", toEmail, e.getMessage());
            throw e;
        }
    }
    
    public void sendPasswordResetCode(String toEmail, String verificationCode) {
        logger.info("Sending password reset code via email to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Code");
            message.setText("Your password reset code is: " + verificationCode + 
                           "\n\nThis code will expire in 10 minutes." +
                           "\n\nIf you didn't request this code, please ignore this email.");
            
            mailSender.send(message);
            logger.info("Password reset code sent successfully via email to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset code via email to: {} - Error: {}", toEmail, e.getMessage());
            throw e;
        }
    }
}
