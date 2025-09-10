package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PurchaseTrxRequest {
    
    @JsonProperty("digest_info")
    private String digestInfo = "NA";
    
    @JsonProperty("device_info")
    private DeviceInfo deviceInfo = new DeviceInfo();
    
    @JsonProperty("request_key")
    private RequestKey requestKey = new RequestKey();
    
    @JsonProperty("request_data")
    private RequestData requestData = new RequestData();
    
    @Data
    public static class DeviceInfo {
        private String name = "NA";
        private String manufacturer = "NA";
        private String model = "NA";
        private String version = "NA";
        private String os = "NA";
    }
    
    @Data
    public static class RequestKey {
        @JsonProperty("request_type")
        private String requestType = "chatcom_card_purchase";
        
        @JsonProperty("request_id")
        private String requestId = "NA";
    }
    
    @Data
    public static class RequestData {
        private String otp = "234562";
        
        @JsonProperty("iso_req_data")
        private IsoReqData isoReqData = new IsoReqData();
    }
    
    @Data
    public static class IsoReqData {
        private String fld11 = "001126";
        private String fld12 = "164936";
        private String fld13 = "0817";
        private String fld14 = "3007";
        private String fld18 = "0601";
        private String fld19 = "850";
        private String fld2 = "6229649600102517";
        private String fld22 = "510";
        private String fld3 = "000000";
        private String fld37 = "499932211227";
        private String fld4 = "000000000202";
        private String fld41 = "00080008";
        private String fld42 = "921059176220008";
        private String fld43 = "VISA TEST                India12      RP";
        private String fld44 = "9876123456";
        private String fld49 = "850";
        private String fld51 = "850";
        private String mti = "0100";
    }
}