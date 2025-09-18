package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class CustomerDataResponse {
    
    @JsonProperty("resp_info")
    private RespInfo respInfo;
    
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
        private RespData respData;
        
        @JsonProperty("resp_desc")
        private String respDesc;
        
        @JsonProperty("resp_status")
        private Integer respStatus;
    }
    
    @Data
    public static class RespData {
        @JsonProperty("card_list")
        private List<Card> cardList;
        
        @JsonProperty("cust_id")
        private String custId;
        
        @JsonProperty("cust_name")
        private String custName;
        
        @JsonProperty("cust_status")
        private String custStatus;
        
        private String gender;
        
        @JsonProperty("inst_id")
        private String instId;
        
        @JsonProperty("origin_country")
        private String originCountry;
        
        @JsonProperty("phn_no_cc")
        private String phnNoCc;
        
        @JsonProperty("primary_mobile_num")
        private String primaryMobileNum;
    }
    
    @Data
    public static class Card {
        private String bin;
        
        @JsonProperty("bin_name")
        private String binName;
        
        @JsonProperty("card_cvv_info")
        private CardCvvInfo cardCvvInfo;
        
        @JsonProperty("card_encoding_type")
        private String cardEncodingType;
        
        @JsonProperty("card_entity_type")
        private String cardEntityType;
        
        @JsonProperty("card_issuance_type")
        private String cardIssuanceType;
        
        @JsonProperty("card_name")
        private String cardName;
        
        @JsonProperty("card_ref_num")
        private String cardRefNum;
        
        @JsonProperty("card_status")
        private String cardStatus;
        
        @JsonProperty("card_status_desc")
        private String cardStatusDesc;
        
        @JsonProperty("card_type")
        private String cardType;
        
        @JsonProperty("encoding_name")
        private String encodingName;
        
        @JsonProperty("exp_date")
        private String expDate;
        
        @JsonProperty("hash_card_num")
        private String hashCardNum;
        
        @JsonProperty("img_data")
        private String imgData;
        
        @JsonProperty("mask_card_num")
        private String maskCardNum;
        
        @JsonProperty("product_id")
        private String productId;
        
        @JsonProperty("product_name")
        private String productName;
        
        @JsonProperty("service_code")
        private String serviceCode;
        
        @JsonProperty("subproduct_id")
        private String subproductId;
        
        @JsonProperty("subproduct_name")
        private String subproductName;
        
        @JsonProperty("wallet_info")
        private WalletInfo walletInfo;
    }
    
    @Data
    public static class CardCvvInfo {
        @JsonProperty("card_ref_num")
        private String cardRefNum;
        
        private String cvv1;
        private String cvv2;
        private String icvv;
        
        @JsonProperty("inst_id")
        private String instId;
    }
    
    @Data
    public static class WalletInfo {
        @JsonProperty("avail_bal")
        private String availBal;
        
        @JsonProperty("commission_bucket")
        private String commissionBucket;
        
        @JsonProperty("discount_bucket")
        private String discountBucket;
        
        @JsonProperty("expense_bucket")
        private String expenseBucket;
        
        @JsonProperty("inst_id")
        private String instId;
        
        @JsonProperty("ledger_bal")
        private String ledgerBal;
        
        @JsonProperty("wallet_currency")
        private String walletCurrency;
        
        @JsonProperty("wallet_num")
        private String walletNum;
        
        @JsonProperty("wallet_product")
        private String walletProduct;
        
        @JsonProperty("wallet_status")
        private String walletStatus;
    }
}
