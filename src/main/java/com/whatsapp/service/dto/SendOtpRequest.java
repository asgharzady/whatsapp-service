package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendOtpRequest {
    
    @JsonProperty("mobileNumber")
    private String mobileNumber;
    
    @JsonProperty("hashKey")
    private String hashKey;
    
    @JsonProperty("phoneCode")
    private String phoneCode;
    
    // Constructors
    public SendOtpRequest() {}
    
    public SendOtpRequest(String mobileNumber, String hashKey, String phoneCode) {
        this.mobileNumber = mobileNumber;
        this.hashKey = hashKey;
        this.phoneCode = phoneCode;
    }
    
    // Getters and Setters
    public String getMobileNumber() {
        return mobileNumber;
    }
    
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
    
    public String getHashKey() {
        return hashKey;
    }
    
    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }
    
    public String getPhoneCode() {
        return phoneCode;
    }
    
    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }
}
