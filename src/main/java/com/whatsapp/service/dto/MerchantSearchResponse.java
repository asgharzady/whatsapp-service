package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class MerchantSearchResponse {
    
    @JsonProperty("resp_info")
    private RespInfo respInfo;
    
    // Constructors
    public MerchantSearchResponse() {}
    
    public MerchantSearchResponse(RespInfo respInfo) {
        this.respInfo = respInfo;
    }
    
    // Getters and Setters
    public RespInfo getRespInfo() {
        return respInfo;
    }
    
    public void setRespInfo(RespInfo respInfo) {
        this.respInfo = respInfo;
    }
    
    // Inner classes
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
        private List<MerchantData> respData;
        
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
        
        public List<MerchantData> getRespData() { return respData; }
        public void setRespData(List<MerchantData> respData) { this.respData = respData; }
        
        public String getRespDesc() { return respDesc; }
        public void setRespDesc(String respDesc) { this.respDesc = respDesc; }
        
        public Integer getRespStatus() { return respStatus; }
        public void setRespStatus(Integer respStatus) { this.respStatus = respStatus; }
    }
    
    public static class MerchantData {
        @JsonProperty("merchant_id")
        private String merchantId;
        
        @JsonProperty("merchant_name")
        private String merchantName;
        
        @JsonProperty("street_name")
        private String streetName;
        
        @JsonProperty("terminal_info")
        private List<TerminalInfo> terminalInfo;
        
        // Constructors
        public MerchantData() {}
        
        // Getters and Setters
        public String getMerchantId() { return merchantId; }
        public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
        
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        
        public String getStreetName() { return streetName; }
        public void setStreetName(String streetName) { this.streetName = streetName; }
        
        public List<TerminalInfo> getTerminalInfo() { return terminalInfo; }
        public void setTerminalInfo(List<TerminalInfo> terminalInfo) { this.terminalInfo = terminalInfo; }
    }
    
    public static class TerminalInfo {
        @JsonProperty("city_name")
        private String cityName;
        
        @JsonProperty("terminal_id")
        private String terminalId;
        
        @JsonProperty("terminal_loc")
        private String terminalLoc;
        
        @JsonProperty("terminal_name")
        private String terminalName;
        
        // Constructors
        public TerminalInfo() {}
        
        // Getters and Setters
        public String getCityName() { return cityName; }
        public void setCityName(String cityName) { this.cityName = cityName; }
        
        public String getTerminalId() { return terminalId; }
        public void setTerminalId(String terminalId) { this.terminalId = terminalId; }
        
        public String getTerminalLoc() { return terminalLoc; }
        public void setTerminalLoc(String terminalLoc) { this.terminalLoc = terminalLoc; }
        
        public String getTerminalName() { return terminalName; }
        public void setTerminalName(String terminalName) { this.terminalName = terminalName; }
    }
}
