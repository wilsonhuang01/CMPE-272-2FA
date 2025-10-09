package com.wilson.cmpe272.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SmsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);
    
    @Value("${twilio.account.sid:mock-sid}")
    private String accountSid;
    
    @Value("${twilio.auth.token:mock-token}")
    private String authToken;
    
    @Value("${twilio.phone.number:+1234567890}")
    private String twilioPhoneNumber;
    
    public void sendVerificationCode(String phoneNumber, String verificationCode) {
        logger.info("Sending SMS verification code to phone: {}", phoneNumber);
        String messageBody = "Your verification code is: " + verificationCode + 
                           ". This code will expire in 10 minutes.";
        
        try {
            // Mock SMS sending - in production, replace with actual SMS provider
            logger.info("SMS MOCK - Sending verification code to {}: {}", phoneNumber, messageBody);
            logger.info("SMS MOCK - Verification code: {}", verificationCode);
            
            // In a real implementation, you would use Twilio or another SMS provider:
            // Twilio.init(accountSid, authToken);
            // Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(twilioPhoneNumber), messageBody).create();
            
            logger.info("SMS verification code sent successfully to: {}", phoneNumber);
        } catch (Exception e) {
            logger.error("Failed to send SMS verification code to: {} - Error: {}", phoneNumber, e.getMessage());
            throw e;
        }
    }
    
    public void sendTwoFactorCode(String phoneNumber, String verificationCode) {
        logger.info("Sending SMS 2FA code to phone: {}", phoneNumber);
        String messageBody = "Your two-factor authentication code is: " + verificationCode + 
                           ". This code will expire in 5 minutes.";
        
        try {
            // Mock SMS sending - in production, replace with actual SMS provider
            logger.info("SMS MOCK - Sending 2FA code to {}: {}", phoneNumber, messageBody);
            logger.info("SMS MOCK - 2FA code: {}", verificationCode);
            
            // In a real implementation, you would use Twilio or another SMS provider:
            // Twilio.init(accountSid, authToken);
            // Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(twilioPhoneNumber), messageBody).create();
            
            logger.info("SMS 2FA code sent successfully to: {}", phoneNumber);
        } catch (Exception e) {
            logger.error("Failed to send SMS 2FA code to: {} - Error: {}", phoneNumber, e.getMessage());
            throw e;
        }
    }
}
