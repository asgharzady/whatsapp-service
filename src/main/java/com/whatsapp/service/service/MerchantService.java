package com.whatsapp.service.service;

import com.whatsapp.service.dto.MerchantSearchRequest;
import com.whatsapp.service.dto.MerchantSearchResponse;
import com.whatsapp.service.dto.PurchaseTrxRequest;
import com.whatsapp.service.dto.PurchaseTrxResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MerchantService {
    
    private static final Logger log = LoggerFactory.getLogger(MerchantService.class);
    
    @Value("${merchant.api.url}")
    private String merchantApiUrl;
    
    @Value("${merchant.purchase.api.url}")
    private String merchantPurchaseApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Search for merchant data using the external API
     * @return MerchantSearchResponse containing the complete response from the API
     */
    public MerchantSearchResponse searchMerchantData() {
        try {
            // Create the fixed request body
            MerchantSearchRequest request = MerchantSearchRequest.createFixedRequest();
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-channel", "web"); // Required header for the API gateway
            headers.set("x-api-lang", "en/US"); // Language header
            headers.set("x-api-version", "1.0.0"); // API version header
            
            // Create HTTP entity with request body and headers
            HttpEntity<MerchantSearchRequest> requestEntity = new HttpEntity<>(request, headers);
            
            log.info("Calling merchant API at: {}", merchantApiUrl);
            log.debug("Request payload: {}", request);
            
            // Make the API call
            ResponseEntity<MerchantSearchResponse> response = restTemplate.exchange(
                merchantApiUrl,
                HttpMethod.POST,
                requestEntity,
                MerchantSearchResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Merchant API call successful. Status: {}", response.getStatusCode());
                log.debug("Response: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Merchant API call failed. Status: {}, Response: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to call merchant API. Status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error calling merchant API: {}", e.getMessage(), e);
            throw new RuntimeException("Error calling merchant API: " + e.getMessage(), e);
        }
    }

    public PurchaseTrxResponse purchaseTrx(PurchaseTrxRequest request) {
        try {
            // Create the request body with default values (automatically set by field defaults)

            
            // Set up headers (same as merchant search)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-channel", "web"); // Required header for the API gateway
            headers.set("x-api-lang", "en/US"); // Language header
            headers.set("x-api-version", "1.0.0"); // API version header
            
            // Create HTTP entity with request body and headers
            HttpEntity<PurchaseTrxRequest> requestEntity = new HttpEntity<>(request, headers);
            
            log.info("Calling merchant purchase API at: {}", merchantPurchaseApiUrl);
            log.debug("Request payload: {}", request);
            
            // Make the API call
            ResponseEntity<PurchaseTrxResponse> response = restTemplate.exchange(
                merchantPurchaseApiUrl,
                HttpMethod.POST,
                requestEntity,
                PurchaseTrxResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Merchant purchase API call successful. Status: {}", response.getStatusCode());
                log.debug("Response: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Merchant purchase API call failed. Status: {}, Response: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to call merchant purchase API. Status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error calling merchant purchase API: {}", e.getMessage(), e);
            throw new RuntimeException("Error calling merchant purchase API: " + e.getMessage(), e);
        }
    }

}
