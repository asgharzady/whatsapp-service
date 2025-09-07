package com.whatsapp.service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class WhatsAppWebhookService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookService.class);
    private static final String EXPECTED_TOKEN = "whatsapp_webhook_token"; // replace with your verify token
    
    // WhatsApp Cloud API configuration
    private static final String WHATSAPP_API_URL = "https://graph.facebook.com/v22.0/809159228940319/messages";
    private static final String ACCESS_TOKEN = "EAAUvTL9hTqsBPWGQZCsw039q0pZClibfrgoE8IwW9ESeimijKyl4gJJgFD01R0iv5F7N4bC4CezOy2NMqU4f8LsZAftlfPVwfuWXXbmvZBjhCdNqt9zU3co7G26ZAiEwZAu4BTzyDIqXCPaFSY3AqmN3oR7yYaWEkBCeNVLMVJqxJMrpJYZBCuzoma1VccBsOMZAStnZCJPA0AJEFgl36KkGc1RahZCXBvlDxaY0w2V2JaKAZDZD";
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();



    public ResponseEntity<String> verifyWebhook(String mode, String verifyToken, String challenge) {
        if ("subscribe".equals(mode) && EXPECTED_TOKEN.equals(verifyToken)) {
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
                    sendWhatsAppMessage(from, "Hi " + userNameStr + ", welcome to AppoPay\n\nSelect Merchant to Pay:\n\n1) Fruita\n\n2) Restaurant 1\n\n3) Restaurant 2\n\n4) Restaurant 3");
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
                String merchantName = "";
                switch (messageText) {
                    case "1":
                        merchantName = "Fruita";
                        break;
                    case "2":
                        merchantName = "Restaurant 1";
                        break;
                    case "3":
                        merchantName = "Restaurant 2";
                        break;
                    case "4":
                        merchantName = "Restaurant 3";
                        break;
                    default:
                        sendWhatsAppMessage(from, "Invalid choice. Please select a merchant:\n\n1) Fruita\n\n2) Restaurant 1\n\n3) Restaurant 2\n\n4) Restaurant 3");
                        return;
                }
                session.setSelectedMerchant(merchantName);
                session.setCurrentState("AMOUNT_ENTRY");
                saveSession(session);
                sendWhatsAppMessage(from, "Hi " + userNameStr + ", welcome to AppoPay\n\nYou Are paying " + merchantName + "\n\nEnter Amount in USD:");
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
            headers.setBearerAuth(ACCESS_TOKEN);
            
            // Create HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(messagePayload, headers);
            
            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                WHATSAPP_API_URL,
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

}
