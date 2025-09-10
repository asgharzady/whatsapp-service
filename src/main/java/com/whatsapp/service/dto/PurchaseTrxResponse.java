package com.whatsapp.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PurchaseTrxResponse {
    
    @JsonProperty("resp_info")
    private RespInfo respInfo;
    
    @Data
    public static class RespInfo {
        @JsonProperty("app_err_desc")
        private String appErrDesc;
        
        @JsonProperty("reject_code")
        private String rejectCode;
        
        @JsonProperty("reject_short_desc")
        private String rejectShortDesc;
        
        @JsonProperty("resp_code")
        private String respCode;
        
        @JsonProperty("resp_data")
        private RespData respData;
        
        @JsonProperty("resp_desc")
        private String respDesc;
        
        @JsonProperty("resp_status")
        private int respStatus;
        
        @JsonProperty("txn_flag")
        private boolean txnFlag;
    }
    
    @Data
    public static class RespData {
        @JsonProperty("acq_info")
        private AcqInfo acqInfo;
        
        @JsonProperty("iss_info")
        private IssInfo issInfo;
        
        @JsonProperty("fwd_info")
        private Object fwdInfo;
        
        @JsonProperty("txf_info")
        private Object txfInfo;
        
        @JsonProperty("sett_info")
        private Object settInfo;
        
        @JsonProperty("card_info")
        private CardInfo cardInfo;
        
        @JsonProperty("cust_info")
        private CustInfo custInfo;
        
        @JsonProperty("corporate_info")
        private Object corporateInfo;
        
        @JsonProperty("client_info")
        private Object clientInfo;
        
        @JsonProperty("driver_info")
        private Object driverInfo;
        
        @JsonProperty("acct_info")
        private AcctInfo acctInfo;
        
        @JsonProperty("wallet_info")
        private Object walletInfo;
        
        @JsonProperty("term_info")
        private TermInfo termInfo;
        
        @JsonProperty("channel_info")
        private ChannelInfo channelInfo;
        
        @JsonProperty("acceptor_info")
        private AcceptorInfo acceptorInfo;
        
        @JsonProperty("store_info")
        private StoreInfo storeInfo;
        
        @JsonProperty("additional_info")
        private Object additionalInfo;
        
        @JsonProperty("txn_info")
        private TxnInfo txnInfo;
        
        @JsonProperty("security_info")
        private Object securityInfo;
        
        @JsonProperty("resp_info")
        private InnerRespInfo respInfo;
        
        @JsonProperty("device_info")
        private Object deviceInfo;
    }
    
    @Data
    public static class AcqInfo {
        private String acquirer;
        
        @JsonProperty("acq_type")
        private String acqType;
        
        @JsonProperty("acq_sub_type")
        private String acqSubType;
        
        @JsonProperty("acq_inst")
        private String acqInst;
        
        @JsonProperty("acq_bin")
        private String acqBin;
        
        @JsonProperty("acq_cntry_code")
        private String acqCntryCode;
        
        @JsonProperty("acq_curr_code")
        private String acqCurrCode;
    }
    
    @Data
    public static class IssInfo {
        @JsonProperty("iss_type")
        private String issType;
        
        @JsonProperty("iss_inst")
        private String issInst;
        
        @JsonProperty("iss_bin")
        private String issBin;
        
        @JsonProperty("iss_cntry_code")
        private String issCntryCode;
        
        @JsonProperty("iss_curr_code")
        private String issCurrCode;
        
        private String issuer;
        
        @JsonProperty("iss_card_product")
        private String issCardProduct;
        
        @JsonProperty("iss_card_subproduct")
        private String issCardSubproduct;
    }
    
    @Data
    public static class CardInfo {
        @JsonProperty("card_num")
        private String cardNum;
        
        @JsonProperty("card_ref_num")
        private String cardRefNum;
        
        @JsonProperty("m_card_num")
        private String mCardNum;
        
        @JsonProperty("e_card_num")
        private String eCardNum;
        
        @JsonProperty("card_seq_num")
        private String cardSeqNum;
        
        @JsonProperty("card_type")
        private String cardType;
        
        @JsonProperty("card_issuance_type")
        private String cardIssuanceType;
        
        @JsonProperty("card_encoding_type")
        private String cardEncodingType;
        
        @JsonProperty("card_entity_type")
        private String cardEntityType;
        
        @JsonProperty("cust_id")
        private String custId;
    }
    
    @Data
    public static class CustInfo {
        @JsonProperty("cust_id")
        private String custId;
        
        @JsonProperty("cust_name")
        private String custName;
        
        @JsonProperty("cust_mobile_num")
        private String custMobileNum;
        
        @JsonProperty("to_cust_name")
        private String toCustName;
    }
    
    @Data
    public static class AcctInfo {
        @JsonProperty("avail_bal")
        private String availBal;
        
        @JsonProperty("ledger_bal")
        private String ledgerBal;
    }
    
    @Data
    public static class TermInfo {
        @JsonProperty("term_channel")
        private String termChannel;
        
        @JsonProperty("term_id")
        private String termId;
        
        @JsonProperty("term_loc")
        private String termLoc;
    }
    
    @Data
    public static class ChannelInfo {
        @JsonProperty("channel_type")
        private String channelType;
        
        @JsonProperty("channel_sub_type")
        private String channelSubType;
        
        @JsonProperty("channel_name")
        private String channelName;
        
        @JsonProperty("channel_version")
        private String channelVersion;
    }
    
    @Data
    public static class AcceptorInfo {
        @JsonProperty("acceptor_id")
        private String acceptorId;
        
        @JsonProperty("acceptor_name")
        private String acceptorName;
        
        private String mcc;
    }
    
    @Data
    public static class StoreInfo {
        @JsonProperty("store_id")
        private String storeId;
    }
    
    @Data
    public static class TxnInfo {
        @JsonProperty("req_id")
        private String reqId;
        
        @JsonProperty("record_num")
        private String recordNum;
        
        private String mti;
        
        @JsonProperty("req_mti")
        private String reqMti;
        
        @JsonProperty("req_pcode")
        private String reqPcode;
        
        private String pcode;
        
        @JsonProperty("txn_type")
        private String txnType;
        
        @JsonProperty("txn_category")
        private String txnCategory;
        
        @JsonProperty("txn_name")
        private String txnName;
        
        @JsonProperty("txn_channel")
        private String txnChannel;
        
        @JsonProperty("acq_txn_amount")
        private String acqTxnAmount;
        
        @JsonProperty("txn_amount")
        private String txnAmount;
        
        @JsonProperty("tran_date")
        private String tranDate;
        
        @JsonProperty("tran_time")
        private String tranTime;
        
        private String stan;
        
        @JsonProperty("local_date")
        private String localDate;
        
        @JsonProperty("local_time")
        private String localTime;
        
        @JsonProperty("expiry_date")
        private String expiryDate;
        
        @JsonProperty("fee_amount")
        private String feeAmount;
        
        private String rrn;
        
        @JsonProperty("auth_code")
        private String authCode;
        
        @JsonProperty("pos_entry_mode")
        private String posEntryMode;
        
        @JsonProperty("pos_cond_code")
        private String posCondCode;
        
        @JsonProperty("txn_activity_list")
        private List<String> txnActivityList;
    }
    
    @Data
    public static class InnerRespInfo {
        @JsonProperty("resp_code")
        private String respCode;
        
        @JsonProperty("resp_desc")
        private String respDesc;
        
        @JsonProperty("reject_code")
        private String rejectCode;
    }
}