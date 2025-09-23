package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class MerchantSearchResponse {
    
    @JsonProperty("resp_info")
    private RespInfo respInfo;
    
    // Constructors
    public MerchantSearchResponse() {}
    
    public MerchantSearchResponse(RespInfo respInfo) {
        this.respInfo = respInfo;
    }
    
    // Inner classes
    @Data
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
    }
    
    @Data
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
    }
    
    @Data
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
    }
}
