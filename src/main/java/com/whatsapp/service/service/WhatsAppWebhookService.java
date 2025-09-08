package com.whatsapp.service.service;

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
import com.whatsapp.service.service.MerchantService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Service
public class WhatsAppWebhookService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookService.class);
    
    // WhatsApp Configuration from application.properties
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

        // Clean up expired sessions first
        cleanupExpiredSessions();

        // Get or create user session
        UserSession session = getUserSession(from);
        
        // Check if session is expired
        if (session.isExpired()) {
            log.info("Session expired for user: {}", from);
            // Reset session to beginning
            session.setCurrentState("LANGUAGE_SELECTION");
            session.setUserName("Moe");
            session.setSelectedMerchant(null);
        }
        
        // Update last activity
        session.updateLastActivity();
        
        String state = session.getCurrentState();
        String userNameStr = session.getUserName() != null ? session.getUserName() : "Moe";
        
        switch (state) {
            case "LANGUAGE_SELECTION":
                if (messageText.equals("1") || messageText.equals("2")) {
                    session.setCurrentState("OTP_ENTRY");
                    session.setUserName("Moe");
                    saveSession(session);
                    sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nEnter your 6 digit OTP sent to your phone number.\n\nor\n\n1) Resend OTP\n\n2) Return Main Menu");
                } else {
                    sendWhatsAppMessage(from, "Invalid choice.\n\n" + getLanguageSelectionMenu());
                }
                break;

            case "OTP_ENTRY":
                // Check if it's a 6-digit OTP or menu option
                if (messageText.matches("\\d{6}")) {
                    // Valid 6-digit OTP
                    session.setCurrentState("MERCHANT_SELECTION");
                    saveSession(session);
                    sendMerchantSelectionButtons(from, userNameStr);
                } else if (messageText.equals("1")) {
                    // Resend OTP option
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
                    sendWhatsAppMessage(from, "Hi " + userNameStr + ", welcome to AppoPay\n\nYou Are paying " + selectedMerchant + "\n\nEnter Amount in USD:");
                } else {
                    sendMerchantSelectionButtons(from, userNameStr);
                    sendWhatsAppMessage(from, "Invalid choice. Please select a merchant from the buttons above.");
                }
                break;

            case "AMOUNT_ENTRY":
                // Validate amount format (basic validation)
                if (messageText.matches("\\d+(\\.\\d{1,2})?")) {
                    session.setCurrentState("CARD_ENTRY");
                    saveSession(session);
                    String merchant = session.getSelectedMerchant();
                    sendWhatsAppMessage(from, "Hi " + userNameStr + ", welcome to AppoPay\n\nYou Are paying " + merchant + "\n\nEnter Your Card number:");
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
                    sendWhatsAppMessage(from, "Hi " + userNameStr + ", welcome to AppoPay\n\nYou Are paying " + merchant + "\n\nEnter Your 6 digit PIN:");
                } else {
                    sendWhatsAppMessage(from, "Invalid card number format. Please enter a valid card number:");
                }
                break;

            case "PIN_ENTRY":
                // Validate PIN format
                if (messageText.matches("\\d{6}")) {
                    session.setCurrentState("PAYMENT_SUCCESS");
                    String merchant = session.getSelectedMerchant();
                    sendWhatsAppMessage(from, "Hi " + userNameStr + ", welcome to AppoPay\n\nYou Are paying " + merchant + "\n\npayment Successful");
                    
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
    
    /**
     * Get or create user session
     */
    private UserSession getUserSession(String phoneNumber) {
        Optional<UserSession> existingSession = userSessionRepository.findByPhoneNumber(phoneNumber);
        
        if (existingSession.isPresent()) {
            return existingSession.get();
        }
        
        // Create new session
        UserSession newSession = new UserSession(phoneNumber, "LANGUAGE_SELECTION");
        newSession.setUserName("Moe");
        return userSessionRepository.save(newSession);
    }
    
    /**
     * Save session to database
     */
    private void saveSession(UserSession session) {
        userSessionRepository.save(session);
    }
    
    /**
     * Clean up expired sessions (older than 5 minutes)
     */
    private void cleanupExpiredSessions() {
        try {
            LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
            userSessionRepository.deleteExpiredSessions(fiveMinutesAgo);
            log.debug("Cleaned up expired sessions older than: {}", fiveMinutesAgo);
        } catch (Exception e) {
            log.error("Error cleaning up expired sessions: {}", e.getMessage(), e);
        }
    }

    private void sendWhatsAppMessage(String to, String text) {
        try {
            // Create message payload for text messages
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("messaging_product", "whatsapp");
            messagePayload.put("to", to);
            messagePayload.put("type", "text");
            
            Map<String, String> textContent = new HashMap<>();
            textContent.put("body", text);
            messagePayload.put("text", textContent);
            
            // Set up headers
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
    
    /**
     * Send a WhatsApp template message (like hello_world template)
     */
    private boolean hasMessage(JsonNode root) {
        JsonNode msgs = root.path("entry").path(0).path("changes").path(0).path("value").path("messages");
        return msgs.isArray() && msgs.size() > 0;
    }

    private String extractFrom(JsonNode root) {
        // entry[0].changes[0].value.messages[0].from
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
    private void sendMerchantSelectionButtons(String to, String userNameStr) {
        try {
            MerchantSearchResponse response = merchantService.searchMerchantData();
            
            if (response != null && response.getRespInfo() != null && 
                response.getRespInfo().getRespData() != null && 
                !response.getRespInfo().getRespData().isEmpty()) {
                
                List<MerchantSearchResponse.MerchantData> merchants = response.getRespInfo().getRespData();
                sendWhatsAppInteractiveButtons(to, userNameStr, merchants);
            } else {
                // No merchants found
                sendWhatsAppMessage(to, "Hi " + userNameStr + ", welcome to AppoPay\n\nNo merchants available at the moment. Please try again later.");
            }
        } catch (Exception e) {
            log.error("Error fetching merchants for buttons: {}", e.getMessage(), e);
            sendWhatsAppMessage(to, "Hi " + userNameStr + ", welcome to AppoPay\n\nNo merchants available at the moment. Please try again later.");
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
     * Send WhatsApp interactive buttons for merchant selection
     */
    private void sendWhatsAppInteractiveButtons(String to, String userNameStr, List<MerchantSearchResponse.MerchantData> merchants) {
        try {
            // Create interactive message payload with buttons
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("messaging_product", "whatsapp");
            messagePayload.put("to", to);
            messagePayload.put("type", "interactive");
            
            // Interactive content
            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "button");
            
            // Body text
            Map<String, String> body = new HashMap<>();
            body.put("text", "Hi " + userNameStr + ", welcome to AppoPay\n\nSelect Merchant to Pay:");
            interactive.put("body", body);
            
            // Header (optional)
            Map<String, String> header = new HashMap<>();
            header.put("type", "text");
            header.put("text", "💳 Merchant Selection");
            interactive.put("header", header);
            
            // Action buttons (max 3 buttons for WhatsApp)
            Map<String, Object> action = new HashMap<>();
            List<Map<String, Object>> buttons = new ArrayList<>();
            
            // Add merchant buttons (limit to 3 due to WhatsApp restrictions)
            int maxButtons = Math.min(merchants.size(), 3);
            for (int i = 0; i < maxButtons; i++) {
                Map<String, Object> button = new HashMap<>();
                button.put("type", "reply");
                
                Map<String, String> reply = new HashMap<>();
                reply.put("id", "merchant_" + i);
                reply.put("title", merchants.get(i).getMerchantName());
                button.put("reply", reply);
                
                buttons.add(button);
            }
            
            action.put("buttons", buttons);
            interactive.put("action", action);
            messagePayload.put("interactive", interactive);
            
            // Set up headers
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
                log.info("Interactive buttons sent successfully to {}", to);
                log.debug("WhatsApp API response: {}", response.getBody());
            } else {
                log.error("Failed to send interactive buttons to {}. Status: {}, Response: {}", 
                    to, response.getStatusCode(), response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Error sending WhatsApp interactive buttons to {}: {}", to, e.getMessage(), e);
            // Fallback to regular text message
            StringBuilder fallbackMenu = new StringBuilder();
            fallbackMenu.append("Hi ").append(userNameStr).append(", welcome to AppoPay\n\nSelect Merchant to Pay:\n\n");
            for (int i = 0; i < merchants.size(); i++) {
                fallbackMenu.append(i + 1).append(") ").append(merchants.get(i).getMerchantName()).append("\n\n");
            }
            sendWhatsAppMessage(to, fallbackMenu.toString().trim());
        }
    }

}
