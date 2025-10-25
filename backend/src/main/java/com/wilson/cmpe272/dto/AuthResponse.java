package com.wilson.cmpe272.dto;

import com.wilson.cmpe272.entity.User;

public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private User.TwoFactorMethod twoFactorMethod;
    private Boolean isTwoFactorEnabled;
    private Boolean requiresTwoFactor;
    private String message;
    
    // Constructors
    public AuthResponse() {}
    
    public AuthResponse(String token, User user) {
        this.token = token;
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.twoFactorMethod = user.getTwoFactorMethod();
        this.isTwoFactorEnabled = user.getIsTwoFactorEnabled();
        this.requiresTwoFactor = user.getIsTwoFactorEnabled() && user.getTwoFactorMethod() != null;
    }

    public AuthResponse(User user, String message) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.twoFactorMethod = user.getTwoFactorMethod();
        this.isTwoFactorEnabled = user.getIsTwoFactorEnabled();
        this.requiresTwoFactor = user.getIsTwoFactorEnabled() && user.getTwoFactorMethod() != null;
        this.message = message;
    }
    
    public AuthResponse(String message) {
        this.message = message;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public User.TwoFactorMethod getTwoFactorMethod() {
        return twoFactorMethod;
    }
    
    public void setTwoFactorMethod(User.TwoFactorMethod twoFactorMethod) {
        this.twoFactorMethod = twoFactorMethod;
    }
    
    public Boolean getIsTwoFactorEnabled() {
        return isTwoFactorEnabled;
    }
    
    public void setIsTwoFactorEnabled(Boolean isTwoFactorEnabled) {
        this.isTwoFactorEnabled = isTwoFactorEnabled;
    }
    
    public Boolean getRequiresTwoFactor() {
        return requiresTwoFactor;
    }
    
    public void setRequiresTwoFactor(Boolean requiresTwoFactor) {
        this.requiresTwoFactor = requiresTwoFactor;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setUser(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.twoFactorMethod = user.getTwoFactorMethod();
        this.isTwoFactorEnabled = user.getIsTwoFactorEnabled();
        this.requiresTwoFactor = user.getIsTwoFactorEnabled() && user.getTwoFactorMethod() != null;
    }
}
