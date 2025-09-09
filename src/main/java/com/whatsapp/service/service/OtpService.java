package com.whatsapp.service.service;

import com.whatsapp.service.dto.AuthResponse;
import com.whatsapp.service.dto.OtpResponse;
import com.whatsapp.service.dto.SendOtpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class OtpService {
    
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    
    @Value("${otp.service.base-url}")
    private String baseUrl;
    
    @Value("${otp.service.username}")
    private String username;
    
    @Value("${otp.service.password}")
    private String password;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public OtpResponse sendOtp(String mobileNumber, String phoneCode) {
        try {
            // Get authentication token
            String token = getAuthToken();
            if (token == null) {
                return new OtpResponse("500", "Failed to authenticate with OTP service");
            }
            // Prepare request
            SendOtpRequest request = new SendOtpRequest(mobileNumber, "", phoneCode);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            
            // Create HTTP entity
            HttpEntity<SendOtpRequest> requestEntity = new HttpEntity<>(request, headers);
            
            String url = baseUrl + "/twilio/sendOTP";
            log.info("Sending OTP to {} with phone code {}", mobileNumber, phoneCode);
            
            // Make the API call
            ResponseEntity<OtpResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                OtpResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("OTP sent successfully to {}", mobileNumber);
                return response.getBody();
            } else {
                log.error("Failed to send OTP. Status: {}", response.getStatusCode());
                return new OtpResponse("500", "Failed to send OTP");
            }
            
        } catch (Exception e) {
            log.error("Error sending OTP to {}: {}", mobileNumber, e.getMessage(), e);
            return new OtpResponse("500", "Error sending OTP: " + e.getMessage());
        }
    }

    public OtpResponse validateOtp(String mobileNumber, String otp) {
        try {
            // Get authentication token
            String token = getAuthToken();
            if (token == null) {
                return new OtpResponse("500", "Failed to authenticate with OTP service");
            }
            
            // URL encode the mobile number to handle special characters like '+'
            String encodedMobileNumber = URLEncoder.encode(mobileNumber, StandardCharsets.UTF_8);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            
            // Create HTTP entity
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            
            String url = baseUrl + "/twilio/validateOTP/" + encodedMobileNumber + "/" + otp;
            log.info("Validating OTP for {} with OTP {}", mobileNumber, otp);
            
            // Make the API call
            ResponseEntity<OtpResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                OtpResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("OTP validation completed for {}: {}", mobileNumber, response.getBody().getMessage());
                return response.getBody();
            } else {
                log.error("Failed to validate OTP. Status: {}", response.getStatusCode());
                return new OtpResponse("500", "Failed to validate OTP");
            }
            
        } catch (Exception e) {
            log.error("Error validating OTP for {}: {}", mobileNumber, e.getMessage(), e);
            return new OtpResponse("500", "Error validating OTP: " + e.getMessage());
        }
    }

    private String getAuthToken() {
        try {
            // Build login URL with query parameters
            String loginUrl = UriComponentsBuilder.fromHttpUrl(baseUrl + "/auth/login")
                .queryParam("username", username)
                .queryParam("password", password)
                .toUriString();
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("accept", "*/*");
            
            // Create HTTP entity
            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            
            log.info("Authenticating with OTP service...");
            
            // Make the authentication call
            ResponseEntity<AuthResponse> response = restTemplate.exchange(
                loginUrl,
                HttpMethod.POST,
                requestEntity,
                AuthResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && 
                response.getBody() != null && 
                "200".equals(response.getBody().getStatus())) {
                
                String token = response.getBody().getMessage();
                log.info("Authentication successful");
                
                return token;
            } else {
                log.error("Authentication failed. Status: {}, Response: {}", 
                    response.getStatusCode(), response.getBody());
                return null;
            }
            
        } catch (Exception e) {
            log.error("Error during authentication: {}", e.getMessage(), e);
            return null;
        }
    }


}
