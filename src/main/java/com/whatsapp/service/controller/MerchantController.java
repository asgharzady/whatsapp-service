package com.whatsapp.service.controller;

import com.whatsapp.service.dto.CustomerDataResponse;
import com.whatsapp.service.dto.DecryptCmsCardResponse;
import com.whatsapp.service.dto.MerchantSearchResponse;
import com.whatsapp.service.service.MerchantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("merchant/")
public class MerchantController {
    
    private static final Logger log = LoggerFactory.getLogger(MerchantController.class);
    
    private final MerchantService merchantService;
    
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }
    
    @PostMapping("search")
    public ResponseEntity<MerchantSearchResponse> searchMerchant() {
        try {
            log.info("Received request to search merchant data");
            
            // Call the merchant service which uses the fixed request body
            MerchantSearchResponse response = merchantService.searchMerchantData();
            
            log.info("Merchant search completed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing merchant search request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("validate-mobile")
    public ResponseEntity<Boolean> validateMobileNumber(@RequestParam String mobileNumber) {
        try {
            log.info("Received request to validate mobile number: {}", mobileNumber);
            
            // Call the merchant service to validate mobile number
            boolean isValid = merchantService.validateMobileNumber(mobileNumber);
            
            log.info("Mobile number {} validation result: {}", mobileNumber, isValid);
            return ResponseEntity.ok(isValid);
            
        } catch (Exception e) {
            log.error("Error processing mobile validation request for {}: {}", mobileNumber, e.getMessage(), e);
            return ResponseEntity.status(500).body(false);
        }
    }

    @PostMapping("fetch-customer-data")
    public ResponseEntity<CustomerDataResponse> fetchCustomerData(
            @RequestParam String phnNoCc,
            @RequestParam String mobileNum,
            @RequestParam String otp) {
        try {
            log.info("Received request to fetch customer data for mobile number: {} with country code: {}", 
                mobileNum, phnNoCc);
            
            // Call the merchant service to fetch customer data
            CustomerDataResponse response = merchantService.fetchCustomerData(phnNoCc, mobileNum, otp);
            
            log.info("Customer data fetch completed successfully for mobile number: {}", mobileNum);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing customer data fetch request for mobile number {} with country code {}: {}", 
                mobileNum, phnNoCc, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("decrypt-cms-card")
    public ResponseEntity<DecryptCmsCardResponse> decryptCmsCardNumber(@RequestParam String cardRefNum) {
        try {
            log.info("Received request to decrypt CMS card number: {}", cardRefNum);
            
            // Call the merchant service to decrypt CMS card number
            DecryptCmsCardResponse response = merchantService.decryptCmsCardNumber(cardRefNum);
            
            log.info("Decrypt CMS card number completed successfully for card ref: {}", cardRefNum);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing decrypt CMS card request for card ref {}: {}", 
                cardRefNum, e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}
