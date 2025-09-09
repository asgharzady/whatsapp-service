package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OtpResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    // Constructors
    public OtpResponse() {}
    
    public OtpResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
