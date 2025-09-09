package com.whatsapp.service.controller;

import com.whatsapp.service.dto.OtpResponse;
import com.whatsapp.service.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("otp/")
public class OtpController {
    
    private static final Logger log = LoggerFactory.getLogger(OtpController.class);
    
    private final OtpService otpService;
    
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("send")
    public ResponseEntity<OtpResponse> sendOtp(
            @RequestParam String mobileNumber,
            @RequestParam(defaultValue = "+92") String phoneCode) {
        
        try {
            log.info("Received request to send OTP to {} with phone code {}", mobileNumber, phoneCode);
            
            OtpResponse response = otpService.sendOtp(mobileNumber, phoneCode);
            
            if ("200".equals(response.getStatus())) {
                log.info("OTP sent successfully to {}", mobileNumber);
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to send OTP to {}: {}", mobileNumber, response.getMessage());
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error processing send OTP request for {}: {}", mobileNumber, e.getMessage(), e);
            return ResponseEntity.status(500).body(new OtpResponse("500", "Internal server error"));
        }
    }

    @GetMapping("validate/{fullMobileNumber}/{otp}")
    public ResponseEntity<OtpResponse> validateOtp(
            @PathVariable String fullMobileNumber,
            @PathVariable String otp) {
        
        try {
            log.info("Received request to validate OTP for {} with OTP {}", fullMobileNumber, otp);
            
            OtpResponse response = otpService.validateOtp(fullMobileNumber, otp);
            
            if ("200".equals(response.getStatus())) {
                boolean isValid = "true".equals(response.getMessage());
                log.info("OTP validation for {}: {}", fullMobileNumber, isValid ? "VALID" : "INVALID");
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to validate OTP for {}: {}", fullMobileNumber, response.getMessage());
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error processing validate OTP request for {}: {}", fullMobileNumber, e.getMessage(), e);
            return ResponseEntity.status(500).body(new OtpResponse("500", "Internal server error"));
        }
    }
}
