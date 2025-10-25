package com.wilson.cmpe272;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wilson.cmpe272.dto.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testSignup() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setConfirmPassword("password123");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SignupRequest> entity = new HttpEntity<>(signupRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup", entity, String.class);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testSignupWithInvalidPassword() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("test2@example.com");
        signupRequest.setPassword("pass"); // Too short
        signupRequest.setConfirmPassword("pass");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SignupRequest> entity = new HttpEntity<>(signupRequest, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/signup", entity, String.class);
        assertEquals(403, response.getStatusCodeValue());
    }
    
    @Test
    public void testLogout() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/logout", entity, String.class);
        // This will return 403 since no authentication is provided, but endpoint exists
        assertEquals(403, response.getStatusCodeValue());
    }
    
    @Test
    public void testGetProfile() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.getForEntity("/api/auth/profile", String.class);
        // This will return 403 since no authentication is provided, but endpoint exists
        assertEquals(403, response.getStatusCodeValue());
    }
}
