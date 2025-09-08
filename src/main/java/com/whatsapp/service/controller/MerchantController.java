package com.whatsapp.service.controller;

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
    
    /**
     * Search for merchant data
     * This endpoint uses a fixed request body internally to call the external API
     * @return ResponseEntity containing the complete merchant search response
     */
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
}
