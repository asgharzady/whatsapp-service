package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MobileValidationResponse {
    
    @JsonProperty("resp_info")
    private RespInfo respInfo;
    
    // Constructors
    public MobileValidationResponse() {}
    
    public MobileValidationResponse(RespInfo respInfo) {
        this.respInfo = respInfo;
    }
    
    // Getters and Setters
    public RespInfo getRespInfo() {
        return respInfo;
    }
    
    public void setRespInfo(RespInfo respInfo) {
        this.respInfo = respInfo;
    }
    
    // Inner class
    public static class RespInfo {
        @JsonProperty("app_corrective_action")
        private String appCorrectiveAction;
        
        @JsonProperty("app_err_desc")
        private String appErrDesc;
        
        @JsonProperty("reject_code")
        private String rejectCode;
        
        @JsonProperty("reject_long_desc")
        private String rejectLongDesc;
        
        @JsonProperty("reject_module")
        private String rejectModule;
        
        @JsonProperty("reject_module_type")
        private String rejectModuleType;
        
        @JsonProperty("reject_short_desc")
        private String rejectShortDesc;
        
        @JsonProperty("resp_code")
        private String respCode;
        
        @JsonProperty("resp_data")
        private RespData respData;
        
        @JsonProperty("resp_desc")
        private String respDesc;
        
        @JsonProperty("resp_status")
        private Integer respStatus;
        
        // Constructors
        public RespInfo() {}
        
        // Getters and Setters
        public String getAppCorrectiveAction() { return appCorrectiveAction; }
        public void setAppCorrectiveAction(String appCorrectiveAction) { this.appCorrectiveAction = appCorrectiveAction; }
        
        public String getAppErrDesc() { return appErrDesc; }
        public void setAppErrDesc(String appErrDesc) { this.appErrDesc = appErrDesc; }
        
        public String getRejectCode() { return rejectCode; }
        public void setRejectCode(String rejectCode) { this.rejectCode = rejectCode; }
        
        public String getRejectLongDesc() { return rejectLongDesc; }
        public void setRejectLongDesc(String rejectLongDesc) { this.rejectLongDesc = rejectLongDesc; }
        
        public String getRejectModule() { return rejectModule; }
        public void setRejectModule(String rejectModule) { this.rejectModule = rejectModule; }
        
        public String getRejectModuleType() { return rejectModuleType; }
        public void setRejectModuleType(String rejectModuleType) { this.rejectModuleType = rejectModuleType; }
        
        public String getRejectShortDesc() { return rejectShortDesc; }
        public void setRejectShortDesc(String rejectShortDesc) { this.rejectShortDesc = rejectShortDesc; }
        
        public String getRespCode() { return respCode; }
        public void setRespCode(String respCode) { this.respCode = respCode; }
        
        public RespData getRespData() { return respData; }
        public void setRespData(RespData respData) { this.respData = respData; }
        
        public String getRespDesc() { return respDesc; }
        public void setRespDesc(String respDesc) { this.respDesc = respDesc; }
        
        public Integer getRespStatus() { return respStatus; }
        public void setRespStatus(Integer respStatus) { this.respStatus = respStatus; }
    }
    
    public static class RespData {
        private String status;
        
        public RespData() {}
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
