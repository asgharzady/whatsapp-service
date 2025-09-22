package com.whatsapp.service.service;

import com.whatsapp.service.dto.CustomerDataRequest;
import com.whatsapp.service.dto.CustomerDataResponse;
import com.whatsapp.service.dto.DecryptCmsCardRequest;
import com.whatsapp.service.dto.DecryptCmsCardResponse;
import com.whatsapp.service.dto.MerchantSearchRequest;
import com.whatsapp.service.dto.MerchantSearchResponse;
import com.whatsapp.service.dto.MobileValidationRequest;
import com.whatsapp.service.dto.MobileValidationResponse;
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


    public boolean validateMobileNumber(String mobileNumber) {
        try {
            // Create the mobile validation request
            MobileValidationRequest request = MobileValidationRequest.createMobileValidationRequest(mobileNumber);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-channel", "web"); // Required header for the API gateway
            headers.set("x-api-lang", "en/US"); // Language header
            headers.set("x-api-version", "1.0.0"); // API version header
            
            // Create HTTP entity with request body and headers
            HttpEntity<MobileValidationRequest> requestEntity = new HttpEntity<>(request, headers);
            
            log.info("Calling mobile validation API at: {} for mobile number: {}", merchantApiUrl, mobileNumber);
            log.debug("Request payload: {}", request);
            
            // Make the API call
            ResponseEntity<MobileValidationResponse> response = restTemplate.exchange(
                merchantApiUrl,
                HttpMethod.POST,
                requestEntity,
                MobileValidationResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                MobileValidationResponse responseBody = response.getBody();
                log.info("Mobile validation API call successful. Status: {}", response.getStatusCode());
                log.debug("Response: {}", responseBody);
                
                // Check if resp_desc is "Approved"
                if (responseBody.getRespInfo() != null && 
                    "Approved".equals(responseBody.getRespInfo().getRespDesc())) {
                    log.info("Mobile number {} validation approved", mobileNumber);
                    return true;
                } else {
                    log.info("Mobile number {} validation not approved. Response desc: {}", 
                        mobileNumber, 
                        responseBody.getRespInfo() != null ? responseBody.getRespInfo().getRespDesc() : "null");
                    return false;
                }
            } else {
                log.error("Mobile validation API call failed. Status: {}, Response: {}", 
                    response.getStatusCode(), response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error calling mobile validation API for mobile number {}: {}", mobileNumber, e.getMessage(), e);
            return false;
        }
    }

    public CustomerDataResponse fetchCustomerData(String phnNoCc, String mobileNum, String otp) {
        try {
            // Create the customer data request
            CustomerDataRequest request = CustomerDataRequest.createCustomerDataRequest(phnNoCc, mobileNum, otp);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-channel", "web"); // Required header for the API gateway
            headers.set("x-api-lang", "en/US"); // Language header
            headers.set("x-api-version", "1.0.0"); // API version header
            
            // Create HTTP entity with request body and headers
            HttpEntity<CustomerDataRequest> requestEntity = new HttpEntity<>(request, headers);
            
            log.info("Calling customer data API at: {} for mobile number: {} with country code: {}", 
                merchantApiUrl, mobileNum, phnNoCc);
            log.debug("Request payload: {}", request);
            
            // Make the API call
            ResponseEntity<CustomerDataResponse> response = restTemplate.exchange(
                merchantApiUrl,
                HttpMethod.POST,
                requestEntity,
                CustomerDataResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Customer data API call successful. Status: {}", response.getStatusCode());
                log.debug("Response: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Customer data API call failed. Status: {}, Response: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to call customer data API. Status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error calling customer data API for mobile number {} with country code {}: {}", 
                mobileNum, phnNoCc, e.getMessage(), e);
            throw new RuntimeException("Error calling customer data API: " + e.getMessage(), e);
        }
    }

    public DecryptCmsCardResponse decryptCmsCardNumber(String cardRefNum) {
        try {
            // Create the decrypt CMS card request
            DecryptCmsCardRequest request = DecryptCmsCardRequest.createDecryptCardRequest(cardRefNum);
            
            // Set up headers (note: using "mob" channel as specified in curl)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-channel", "mob"); // Required header for the API gateway
            headers.set("x-api-lang", "en/US"); // Language header
            headers.set("x-api-version", "1.0.0"); // API version header
            
            // Create HTTP entity with request body and headers
            HttpEntity<DecryptCmsCardRequest> requestEntity = new HttpEntity<>(request, headers);
            
            log.info("Calling decrypt CMS card API at: {} for card reference number: {}", 
                merchantApiUrl, cardRefNum);
            log.debug("Request payload: {}", request);
            
            // Make the API call
            ResponseEntity<DecryptCmsCardResponse> response = restTemplate.exchange(
                merchantApiUrl,
                HttpMethod.POST,
                requestEntity,
                DecryptCmsCardResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Decrypt CMS card API call successful. Status: {}", response.getStatusCode());
                log.debug("Response: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Decrypt CMS card API call failed. Status: {}, Response: {}", 
                    response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to call decrypt CMS card API. Status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("Error calling decrypt CMS card API for card reference number {}: {}", 
                cardRefNum, e.getMessage(), e);
            throw new RuntimeException("Error calling decrypt CMS card API: " + e.getMessage(), e);
        }
    }

}
