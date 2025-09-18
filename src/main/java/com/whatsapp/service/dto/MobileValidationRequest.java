package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MobileValidationRequest {
    
    @JsonProperty("digest_info")
    private String digestInfo;
    
    @JsonProperty("device_info")
    private DeviceInfo deviceInfo;
    
    @JsonProperty("request_key")
    private RequestKey requestKey;
    
    @JsonProperty("request_data")
    private RequestData requestData;
    
    // Constructors
    public MobileValidationRequest() {}
    
    public MobileValidationRequest(String digestInfo, DeviceInfo deviceInfo, RequestKey requestKey, RequestData requestData) {
        this.digestInfo = digestInfo;
        this.deviceInfo = deviceInfo;
        this.requestKey = requestKey;
        this.requestData = requestData;
    }
    
    // Static factory method to create request with mobile number
    public static MobileValidationRequest createMobileValidationRequest(String mobileNumber) {
        DeviceInfo deviceInfo = new DeviceInfo("NA", "NA", "NA", "NA", "NA");
        RequestKey requestKey = new RequestKey("mobile_app_cust_validation", "NA");
        RequestData requestData = new RequestData("AP", mobileNumber);
        
        return new MobileValidationRequest("NA", deviceInfo, requestKey, requestData);
    }
    
    // Getters and Setters
    public String getDigestInfo() {
        return digestInfo;
    }
    
    public void setDigestInfo(String digestInfo) {
        this.digestInfo = digestInfo;
    }
    
    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public RequestKey getRequestKey() {
        return requestKey;
    }
    
    public void setRequestKey(RequestKey requestKey) {
        this.requestKey = requestKey;
    }
    
    public RequestData getRequestData() {
        return requestData;
    }
    
    public void setRequestData(RequestData requestData) {
        this.requestData = requestData;
    }
    
    // Inner classes
    public static class DeviceInfo {
        private String name;
        private String manufacturer;
        private String model;
        private String version;
        private String os;
        
        public DeviceInfo() {}
        
        public DeviceInfo(String name, String manufacturer, String model, String version, String os) {
            this.name = name;
            this.manufacturer = manufacturer;
            this.model = model;
            this.version = version;
            this.os = os;
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
        
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getOs() { return os; }
        public void setOs(String os) { this.os = os; }
    }
    
    public static class RequestKey {
        @JsonProperty("request_type")
        private String requestType;
        
        @JsonProperty("request_id")
        private String requestId;
        
        public RequestKey() {}
        
        public RequestKey(String requestType, String requestId) {
            this.requestType = requestType;
            this.requestId = requestId;
        }
        
        public String getRequestType() { return requestType; }
        public void setRequestType(String requestType) { this.requestType = requestType; }
        
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
    }
    
    public static class RequestData {
        @JsonProperty("inst_id")
        private String instId;
        
        @JsonProperty("mobile_num")
        private String mobileNum;
        
        public RequestData() {}
        
        public RequestData(String instId, String mobileNum) {
            this.instId = instId;
            this.mobileNum = mobileNum;
        }
        
        public String getInstId() { return instId; }
        public void setInstId(String instId) { this.instId = instId; }
        
        public String getMobileNum() { return mobileNum; }
        public void setMobileNum(String mobileNum) { this.mobileNum = mobileNum; }
    }
}
