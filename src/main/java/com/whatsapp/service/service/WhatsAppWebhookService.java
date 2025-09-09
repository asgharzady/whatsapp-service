package com.whatsapp.service.service;

import com.whatsapp.service.dto.OtpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.whatsapp.service.entity.UserSession;
import com.whatsapp.service.repository.UserSessionRepository;
import com.whatsapp.service.dto.MerchantSearchResponse;
    import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Service
public class WhatsAppWebhookService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookService.class);
    @Value("${whatsapp.webhook.expected-token}")
    private String expectedToken;
    @Value("${whatsapp.api.url}")
    private String whatsappApiUrl;
    @Value("${whatsapp.api.access-token}")
    private String accessToken;
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private OtpService otpService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> verifyWebhook(String mode, String verifyToken, String challenge) {
        if ("subscribe".equals(mode) && expectedToken.equals(verifyToken)) {
            log.info("Webhook verified successfully.");
            return ResponseEntity.ok(challenge);
        } else {
            log.warn("Webhook verification failed. Mode: {}, Token: {}", mode, verifyToken);
            return ResponseEntity.status(403).body("Verification failed");
        }
    }

    @SuppressWarnings("unchecked")
    public void processIncomingMessage(Map<String, Object> payload) {
        JsonNode root = objectMapper.valueToTree(payload);

        // If this is not a message event (e.g., delivery/read statuses), ignore gracefully
        if (!hasMessage(root)) {
            log.info("Received non-message webhook (likely statuses). Ignoring.\n{}", safePretty(root));
            return;
        }
        String from = extractFrom(root);
        String messageText = extractMessageText(root);
        if (from == null || messageText == null) {
            log.warn("Malformed message payload. from={} text={}\n{}", from, messageText, safePretty(root));
            return;
        }
        log.info("From: {}, Message: {}", from, messageText);

        UserSession session = getUserSession(from);
        
        // Check if session is expired
        if (session.isExpired()) {
            log.info("Session expired for user: {}", from);
            // Reset session to beginning
            session.setCurrentState("LANGUAGE_SELECTION");
            session.setSelectedMerchant(null);
        }
        
        // Update last activity
        session.updateLastActivity();
        
        String state = session.getCurrentState();
        
        switch (state) {
            case "LANGUAGE_SELECTION":
                if (messageText.equals("1") || messageText.equals("2")) {
                    session.setCurrentState("OTP_ENTRY");
                    saveSession(session);
                    log.info("frommmmmm");
                    log.info(from);
                    System.out.println("sout from");
                    otpService.sendOtpWithWhatsappNo(from);
                    sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nEnter your 6 digit OTP sent to your phone number.\n\nor\n\n1) Resend OTP\n\n2) Return Main Menu");
                } else {
                    sendWhatsAppMessage(from, "Invalid choice.\n\n" + getLanguageSelectionMenu());
                }
                break;

            case "OTP_ENTRY":
                // Check if it's a 6-digit OTP or menu option
                if (messageText.matches("\\d{6}")) {
                    String fullPhoneNumber = formatWhatsAppNumberForValidation(from);
                    OtpResponse otpValidationResult = otpService.validateOtp(fullPhoneNumber, messageText);
                    
                    if ("200".equals(otpValidationResult.getStatus()) && "true".equals(otpValidationResult.getMessage())) {
                        log.info("OTP validation successful for user: {}", from);
                        session.setCurrentState("MERCHANT_SELECTION");
                        saveSession(session);
                        sendMerchantSelectionButtons(from);
                    } else {
                        log.warn("OTP validation failed for user: {}", from);
                        sendWhatsAppMessage(from, "Invalid OTP. Please try again.\n\nEnter your 6 digit OTP sent to your phone number.\n\nor\n\n1) Resend OTP\n\n2) Return Main Menu");
                    }
                } else if (messageText.equals("1")) {
                    otpService.sendOtpWithWhatsappNo(from);
                    sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nOTP has been resent to your phone number.\n\nEnter your 6 digit OTP sent to your phone number.\n\nor\n\n1) Resend OTP\n\n2) Return Main Menu");
                } else if (messageText.equals("2")) {
                    // Return to main menu
                    session.setCurrentState("LANGUAGE_SELECTION");
                    saveSession(session);
                    sendWhatsAppMessage(from, getLanguageSelectionMenu());
                } else {
                    sendWhatsAppMessage(from, "Invalid input. Please enter a 6-digit OTP or select:\n\n1) Resend OTP\n\n2) Return Main Menu");
                }
                break;

            case "MERCHANT_SELECTION":
                String selectedMerchant = selectMerchantByName(messageText);
                if (selectedMerchant != null) {
                    session.setSelectedMerchant(selectedMerchant);
                    session.setCurrentState("AMOUNT_ENTRY");
                    saveSession(session);
                    sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nYou Are paying " + selectedMerchant + "\n\nEnter Amount in USD:");
                } else {
                    sendMerchantSelectionButtons(from);
                    sendWhatsAppMessage(from, "Invalid choice. Please select a merchant from the buttons above.");
                }
                break;

            case "AMOUNT_ENTRY":
                // Validate amount format (basic validation)
                if (messageText.matches("\\d+(\\.\\d{1,2})?")) {
                    session.setCurrentState("CARD_ENTRY");
                    saveSession(session);
                    String merchant = session.getSelectedMerchant();
                    sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nYou Are paying " + merchant + "\n\nEnter Your Card number:");
                } else {
                    sendWhatsAppMessage(from, "Invalid amount format. Please enter a valid amount in USD (e.g., 10.50):");
                }
                break;

            case "CARD_ENTRY":
                // Validate card number format (basic validation)
                if (messageText.matches("\\d{13,19}")) {
                    session.setCurrentState("PIN_ENTRY");
                    saveSession(session);
                    String merchant = session.getSelectedMerchant();
                    sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nYou Are paying " + merchant + "\n\nEnter Your 6 digit PIN:");
                } else {
                    sendWhatsAppMessage(from, "Invalid card number format. Please enter a valid card number:");
                }
                break;

            case "PIN_ENTRY":
                // Validate PIN format
                if (messageText.matches("\\d{6}")) {
                    session.setCurrentState("PAYMENT_SUCCESS");
                    String merchant = session.getSelectedMerchant();
                    sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nYou Are paying " + merchant + "\n\npayment Successful");
                    
                    // Auto-transition back to language selection after payment success
                    session.setCurrentState("LANGUAGE_SELECTION");
                    session.setSelectedMerchant(null);
                    saveSession(session);
                    sendWhatsAppMessage(from, getLanguageSelectionMenu());
                } else {
                    sendWhatsAppMessage(from, "Invalid PIN format. Please enter a 6-digit PIN:");
                }
                break;

            case "PAYMENT_SUCCESS":
                // This state automatically transitions back to language selection
                session.setCurrentState("LANGUAGE_SELECTION");
                session.setSelectedMerchant(null);
                saveSession(session);
                sendWhatsAppMessage(from, getLanguageSelectionMenu());
                break;

            default:
                session.setCurrentState("LANGUAGE_SELECTION");
                saveSession(session);
                sendWhatsAppMessage(from, getLanguageSelectionMenu());
        }
    }
    private String getLanguageSelectionMenu() {
        return "Hi, welcome to AppoPay\n\n\n\nlanguage Selection\n\n1) English\n\n2) Spanish";
    }
    private UserSession getUserSession(String phoneNumber) {
        Optional<UserSession> existingSession = userSessionRepository.findByPhoneNumber(phoneNumber);
        
        if (existingSession.isPresent()) {
            return existingSession.get();
        }
        
        // Create new session
        UserSession newSession = new UserSession(phoneNumber, "LANGUAGE_SELECTION");
        return userSessionRepository.save(newSession);
    }

    private void saveSession(UserSession session) {
        userSessionRepository.save(session);
    }

    private void sendWhatsAppMessage(String to, String text) {
        try {
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("messaging_product", "whatsapp");
            messagePayload.put("to", to);
            messagePayload.put("type", "text");
            
            Map<String, String> textContent = new HashMap<>();
            textContent.put("body", text);
            messagePayload.put("text", textContent);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(messagePayload, headers);
            
            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                whatsappApiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Message sent successfully to {}: {}", to, text);
                log.debug("WhatsApp API response: {}", response.getBody());
            } else {
                log.error("Failed to send message to {}. Status: {}, Response: {}", 
                    to, response.getStatusCode(), response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Error sending WhatsApp message to {}: {}", to, e.getMessage(), e);
        }
    }

    private boolean hasMessage(JsonNode root) {
        JsonNode msgs = root.path("entry").path(0).path("changes").path(0).path("value").path("messages");
        return msgs.isArray() && msgs.size() > 0;
    }

    private String extractFrom(JsonNode root) {
        JsonNode from = root.path("entry").path(0).path("changes").path(0).path("value").path("messages").path(0).path("from");
        return from.isMissingNode() ? null : from.asText(null);
    }

    private String extractMessageText(JsonNode root) {
        JsonNode message = root.path("entry").path(0).path("changes").path(0).path("value").path("messages").path(0);

        // type can be "text", "interactive", "button", etc.
        String type = message.path("type").asText("");

        if ("text".equals(type)) {
            return message.path("text").path("body").asText(null);
        }

        // Handle interactive replies (buttons/lists)
        if ("interactive".equals(type)) {
            // button reply
            JsonNode btn = message.path("interactive").path("button_reply").path("title");
            if (!btn.isMissingNode()) return btn.asText();

            // list reply
            JsonNode list = message.path("interactive").path("list_reply").path("title");
            if (!list.isMissingNode()) return list.asText();
        }

        // Fallback: try to read whatever body might exist
        JsonNode body = message.path("text").path("body");
        return body.isMissingNode() ? null : body.asText(null);
    }

    private String safePretty(JsonNode n) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(n);
        } catch (Exception e) {
            return n.toString();
        }
    }

    /**
     * Send merchant selection as interactive buttons
     */
    private void sendMerchantSelectionButtons(String to) {
        try {
            log.info("Attempting to send merchant selection to: {}", to);
            MerchantSearchResponse response = merchantService.searchMerchantData();
            
            if (response != null && response.getRespInfo() != null && 
                response.getRespInfo().getRespData() != null && 
                !response.getRespInfo().getRespData().isEmpty()) {
                
                List<MerchantSearchResponse.MerchantData> merchants = response.getRespInfo().getRespData();
                log.info("Found {} merchants, sending interactive list", merchants.size());
                sendWhatsAppInteractiveList(to, merchants);
            } else {
                // No merchants found
                log.warn("No merchants found in API response");
                sendWhatsAppMessage(to, "Hi, welcome to AppoPay\n\nNo merchants available at the moment. Please try again later.");
            }
        } catch (Exception e) {
            log.error("Error fetching merchants for buttons: {}", e.getMessage(), e);
            sendWhatsAppMessage(to, "Hi, welcome to AppoPay\n\nNo merchants available at the moment. Please try again later.");
        }
    }

    /**
     * Select merchant by name from the API response (for button selections)
     */
    private String selectMerchantByName(String merchantName) {
        try {
            MerchantSearchResponse response = merchantService.searchMerchantData();
            
            if (response != null && response.getRespInfo() != null && 
                response.getRespInfo().getRespData() != null && 
                !response.getRespInfo().getRespData().isEmpty()) {
                
                List<MerchantSearchResponse.MerchantData> merchants = response.getRespInfo().getRespData();
                for (MerchantSearchResponse.MerchantData merchant : merchants) {
                    if (merchant.getMerchantName().equals(merchantName)) {
                        return merchant.getMerchantName();
                    }
                }
            }
            // No merchants available or merchant not found
        } catch (Exception e) {
            log.error("Error selecting merchant by name: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Send WhatsApp interactive list for merchant selection
     */
    private void sendWhatsAppInteractiveList(String to, List<MerchantSearchResponse.MerchantData> merchants) {
        try {
            // Create interactive message payload with list
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("messaging_product", "whatsapp");
            messagePayload.put("to", to);
            messagePayload.put("type", "interactive");
            
            // Interactive content
            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "list");
            
            // Body text
            Map<String, String> body = new HashMap<>();
            body.put("text", "Hi, welcome to AppoPay\n\nSelect a merchant to pay:");
            interactive.put("body", body);
            
            // Header (optional)
            Map<String, String> header = new HashMap<>();
            header.put("type", "text");
            header.put("text", "💳 Merchant Selection");
            interactive.put("header", header);
            
            // Action with list
            Map<String, Object> action = new HashMap<>();
            action.put("button", "View Merchants");
            
            // List sections (can have multiple sections, each with up to 10 rows)
            List<Map<String, Object>> sections = new ArrayList<>();
            Map<String, Object> section = new HashMap<>();
            section.put("title", "Available Merchants");
            
            // List rows (max 10 merchants per list)
            List<Map<String, Object>> rows = new ArrayList<>();
            int maxMerchants = Math.min(merchants.size(), 10);
            
            for (int i = 0; i < maxMerchants; i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", "merchant_" + i);
                
                // Truncate merchant name to 24 characters (WhatsApp limit)
                String merchantName = merchants.get(i).getMerchantName();
                String truncatedName = merchantName.length() > 24 ? merchantName.substring(0, 24) : merchantName;
                row.put("title", truncatedName);
                
                // Add description with full merchant name and street name if available
                String description = "Select to pay " + merchantName;
                if (merchants.get(i).getStreetName() != null && !merchants.get(i).getStreetName().isEmpty()) {
                    description = merchants.get(i).getStreetName() + " - " + merchantName;
                }
                // Truncate description to 72 characters (WhatsApp limit)
                if (description.length() > 72) {
                    description = description.substring(0, 72);
                }
                row.put("description", description);
                
                rows.add(row);
            }
            
            section.put("rows", rows);
            sections.add(section);
            action.put("sections", sections);
            
            interactive.put("action", action);
            messagePayload.put("interactive", interactive);
            
            log.info("Interactive list payload created for {}", to);
            log.debug("Payload: {}", messagePayload);
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(messagePayload, headers);
            
            log.info("Sending interactive list to WhatsApp API...");
            
            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                whatsappApiUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
            );
            
            log.info("WhatsApp API response status: {}", response.getStatusCode());
            log.info("WhatsApp API response body: {}", response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Interactive list sent successfully to {}", to);
            } else {
                log.info("Failed to send interactive list to {}. Status: {}, Response: {}",
                    to, response.getStatusCode(), response.getBody());
                throw new RuntimeException("WhatsApp API rejected interactive list");
            }
            
        } catch (Exception e) {
            log.info("Error sending WhatsApp interactive list to {}: {}", to, e.getMessage(), e);
            // Fallback to regular text message
            StringBuilder fallbackMenu = new StringBuilder();
            fallbackMenu.append("Hi, welcome to AppoPay\n\nSelect Merchant to Pay:\n\n");
            for (int i = 0; i < merchants.size(); i++) {
                fallbackMenu.append(i + 1).append(") ").append(merchants.get(i).getMerchantName()).append("\n\n");
            }
            sendWhatsAppMessage(to, fallbackMenu.toString().trim());
        }
    }
    private String formatWhatsAppNumberForValidation(String whatsappNumber) {
        return "+" + whatsappNumber;
    }

}
