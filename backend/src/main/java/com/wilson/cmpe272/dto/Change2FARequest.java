package com.wilson.cmpe272.dto;

import com.wilson.cmpe272.entity.User;
import jakarta.validation.constraints.NotBlank;

public class Change2FARequest {
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private User.TwoFactorMethod newTwoFactorMethod;
    private String phoneNumber;
    private String twoFactorCode;
    
    // Constructors
    public Change2FARequest() {}
    
    public Change2FARequest(String password, User.TwoFactorMethod newTwoFactorMethod) {
        this.password = password;
        this.newTwoFactorMethod = newTwoFactorMethod;
    }
    
    // Getters and Setters
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public User.TwoFactorMethod getNewTwoFactorMethod() {
        return newTwoFactorMethod;
    }
    
    public void setNewTwoFactorMethod(User.TwoFactorMethod newTwoFactorMethod) {
        this.newTwoFactorMethod = newTwoFactorMethod;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getTwoFactorCode() {
        return twoFactorCode;
    }
    
    public void setTwoFactorCode(String twoFactorCode) {
        this.twoFactorCode = twoFactorCode;
    }
}
