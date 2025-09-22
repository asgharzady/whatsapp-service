package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DecryptCmsCardResponse {
    
    @JsonProperty("resp_info")
    private RespInfo respInfo;
    
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
        private RespData respData;
        
        @JsonProperty("resp_desc")
        private String respDesc;
        
        @JsonProperty("resp_status")
        private Integer respStatus;
    }
    
    @Data
    public static class RespData {
        @JsonProperty("d_card_num")
        private String dCardNum;
    }
}
