package com.whatsapp.service.service;

import com.whatsapp.service.dto.*;
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
import com.whatsapp.service.util.WhatsAppUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@Service
public class WhatsAppWebhookService {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${whatsapp.webhook.expected-token}")
    private String expectedToken;
    @Value("${whatsapp.api.url}")
    private String whatsappApiUrl;
    @Value("${whatsapp.api.access-token}")
    private String accessToken;
    @Value("${server.base-url}")
    private String serverBaseUrl;
    @Autowired
    private UserSessionRepository userSessionRepository;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private PdfService pdfService;

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
        if (!hasMessage(root)) {
            log.info("Received non-message webhook (likely statuses). Ignoring.\n{}", safePretty(root));
            return;
        }
        String from = extractFrom(root);
        if (!merchantService.validateMobileNumber(WhatsAppUtils.parseWhatsAppPhoneNumber(from)[1])){
            sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nPlease register with your mobile number.");
            return;
        }

        String messageText = extractMessageText(root);
        if (from == null || messageText == null) {
            log.warn("Malformed message payload. from={} text={}\n{}", from, messageText, safePretty(root));
            return;
        }
        log.info("From: {}, Message: {}", from, messageText);

        UserSession session = getUserSession(from);
        if (session.isExpired()) {
            log.info("Session expired for user: {}", from);
            // Reset session to beginning
            session.setCurrentState("LANGUAGE_SELECTION");
            session.setSelectedMerchant(null);
        }

        session.updateLastActivity();
        String state = session.getCurrentState();

        switch (state) {
            case "LANGUAGE_SELECTION":
                if (messageText.equals("1") || messageText.equals("2")) {
                    session.setCurrentState("OTP_ENTRY");
                    saveSession(session);
                    log.info("frommmmmm");
                    log.info(from);
                    otpService.sendOtpWithWhatsappNo(from);
                    sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nEnter your 6 digit OTP sent to your phone number.\n\nor\n\n1) Resend OTP\n\n2) Return Main Menu");
                } else {
                    sendWhatsAppMessage(from, getLanguageSelectionMenu());
                }
                break;

            case "OTP_ENTRY":
                if (messageText.matches("\\d{6}")) {
                    String fullPhoneNumber = formatWhatsAppNumberForValidation(from);
                    OtpResponse otpValidationResult = otpService.validateOtp(fullPhoneNumber, messageText);
                    if ("200".equals(otpValidationResult.getStatus()) && "true".equals(otpValidationResult.getMessage())) {
                        log.info("OTP validation successful for user: {}", from);
                        session.setCurrentState("CARD_SELECTION");
                        saveSession(session);
                        sendCardSelectionButtons(from);
                    } else {
                        log.warn("OTP validation failed for user: {}", from);
                        sendWhatsAppMessage(from, "Invalid OTP. Please try again.\n\nEnter your 6 digit OTP sent to your phone number.\n\nor\n\n1) Resend OTP\n\n2) Return Main Menu");
                    }
                } else if (messageText.equals("1")) {
                    otpService.sendOtpWithWhatsappNo(from);
                    sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nOTP has been resent to your phone number.\n\nEnter your 6 digit OTP sent to your phone number.\n\nor\n\n1) Resend OTP\n\n2) Return Main Menu");
                } else if (messageText.equals("2")) {
                    session.setCurrentState("LANGUAGE_SELECTION");
                    saveSession(session);
                    sendWhatsAppMessage(from, getLanguageSelectionMenu());
                } else {
                    sendWhatsAppMessage(from, "Invalid input. Please enter a 6-digit OTP or select:\n\n1) Resend OTP\n\n2) Return Main Menu");
                }
                break;

            case "CARD_SELECTION":
                String selectedCard = messageText;
                session.setSelectedCard(selectedCard);
                session.setCurrentState("MERCHANT_SELECTION");
                saveSession(session);
                sendMerchantSelectionButtons(from);
                break;

            case "MERCHANT_SELECTION":
                String selectedMerchant = selectMerchantByName(messageText);
                session.setSelectedMerchant(selectedMerchant);
                session.setCurrentState("AMOUNT_ENTRY");
                saveSession(session);
                sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nYou Are paying " + selectedMerchant + "\n\nEnter Amount in USD:");
                break;

            case "AMOUNT_ENTRY":
                if (messageText.matches("\\d+(\\.\\d{1,2})?")) {
                    session.setCurrentState("LANGUAGE_SELECTION");
                    session.setAmount(Long.parseLong(messageText));
                    saveSession(session);
                    boolean transactionSuccess = purchaseTrx(session);
                    if (transactionSuccess) {
                        String merchant = session.getSelectedMerchant();
                        String amount = String.valueOf(session.getAmount()); // You can get this from session or transaction response

                        String pdfId = pdfService.generateReceiptPDF(session, amount);
                        if (pdfId != null) {
                            String filename = "receipt_" + session.getPhoneNumber() + ".pdf";
                            String caption = "Your receipt from " + (merchant != null ? merchant : "AppoPay") +
                                    "\nAmount: " + amount +
                                    "\nThank you for using AppoPay!";

                            sendWhatsAppDocument(from, pdfId, filename, caption);
                            sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nPayment to " + merchant + " successful!\n\nYour receipt has been sent as a PDF document.");
                        } else {
                            sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nPayment to " + merchant + " successful!\n\nReceipt generation failed, but your payment was processed successfully.");
                        }
                    } else {
                        String merchant = session.getSelectedMerchant();
                        sendWhatsAppMessage(from, "Hi, welcome to AppoPay\n\nPayment to " + merchant + " failed. Please try again.");
                        return;
                    }
                } else {
                    sendWhatsAppMessage(from, "Invalid amount format. Please enter a valid amount in USD (e.g., 10.50):");
                }
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
            ResponseEntity<String> response = restTemplate.exchange(whatsappApiUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Message sent successfully to {}: {}", to, text);
                log.debug("WhatsApp API response: {}", response.getBody());
            } else {
                log.error("Failed to send message to {}. Status: {}, Response: {}", to, response.getStatusCode(), response.getBody());
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

    private void sendMerchantSelectionButtons(String to) {
        try {
            log.info("Attempting to send merchant selection to: {}", to);
            MerchantSearchResponse response = merchantService.searchMerchantData();
            if (response != null && response.getRespInfo() != null && response.getRespInfo().getRespData() != null && !response.getRespInfo().getRespData().isEmpty()) {
                List<MerchantSearchResponse.MerchantData> merchants = response.getRespInfo().getRespData();
                log.info("Found {} merchants, sending interactive list", merchants.size());
                sendWhatsAppInteractiveListForMerchants(to, merchants);
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

    private void sendCardSelectionButtons(String to) {
        try {
            log.info("Attempting to send card selection to: {}", to);
            String[] phoneData = WhatsAppUtils.parseWhatsAppPhoneNumber(to);
            String countryCode = phoneData[0];
            String mobileNo = phoneData[1];
            CustomerDataResponse response = merchantService.fetchCustomerData(countryCode, mobileNo, "429267");
            
            if (response != null && response.getRespInfo() != null && response.getRespInfo().getRespData() != null 
                && response.getRespInfo().getRespData().getCardList() != null && !response.getRespInfo().getRespData().getCardList().isEmpty()) {
                // Filter only active cards
                List<CustomerDataResponse.Card> activeCards = response.getRespInfo().getRespData().getCardList()
                    .stream()
                    .filter(card -> "Active".equalsIgnoreCase(card.getCardStatusDesc()))
                    .toList();
                if (!activeCards.isEmpty()) {
                    log.info("Found {} active cards, sending interactive list", activeCards.size());
                    sendWhatsAppInteractiveListForCards(to, activeCards);
                } else {
                    log.warn("No active cards found for user: {}", to);
                    sendWhatsAppMessage(to, "No active cards found in your account. Please contact support for assistance.");
                }
            } else {
                // No cards found
                log.warn("No cards found in API response for user: {}", to);
                sendWhatsAppMessage(to, "Unable to retrieve your cards at the moment. Please try again later.");
            }
        } catch (Exception e) {
            log.error("Error fetching cards for user {}: {}", to, e.getMessage(), e);
            sendWhatsAppMessage(to, "Unable to retrieve your cards at the moment. Please try again later.");
        }
    }

    private String selectMerchantByName(String merchantName) {
        try {
            MerchantSearchResponse response = merchantService.searchMerchantData();

            if (response != null && response.getRespInfo() != null && response.getRespInfo().getRespData() != null && !response.getRespInfo().getRespData().isEmpty()) {

                List<MerchantSearchResponse.MerchantData> merchants = response.getRespInfo().getRespData();
                for (MerchantSearchResponse.MerchantData merchant : merchants) {
                    if (merchant.getMerchantName().equals(merchantName) || 
                        WhatsAppUtils.truncateMerchantNameForTitle(merchant.getMerchantName()).equals(merchantName)) {
                        return merchant.getMerchantName();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error selecting merchant by name: {}", e.getMessage(), e);
        }
        return null;
    }
    private void sendWhatsAppInteractiveListForMerchants(String to, List<MerchantSearchResponse.MerchantData> merchants) {
        List<Map<String, String>> listItems = new ArrayList<>();
        
        int maxMerchants = Math.min(merchants.size(), 10);
        for (int i = 0; i < maxMerchants; i++) {
            Map<String, String> item = new HashMap<>();
            item.put("id", "merchant_" + i);
            item.put("title", WhatsAppUtils.truncateMerchantNameForTitle(merchants.get(i).getMerchantName()));
            item.put("description", WhatsAppUtils.createMerchantDescription(
                merchants.get(i).getMerchantName(), 
                merchants.get(i).getStreetName()
            ));
            listItems.add(item);
        }
        
        sendWhatsAppGenericInteractiveList(to, "💳 Merchant Selection", "Hi, welcome to AppoPay\n\nSelect a merchant to pay:", 
            "View Merchants", "Available Merchants", listItems, "merchant");
    }

    private void sendWhatsAppInteractiveListForCards(String to, List<CustomerDataResponse.Card> cards) {
        List<Map<String, String>> listItems = new ArrayList<>();
        
        int maxCards = Math.min(cards.size(), 10);
        for (int i = 0; i < maxCards; i++) {
            CustomerDataResponse.Card card = cards.get(i);
            Map<String, String> item = new HashMap<>();
            item.put("id", "card_" + i);
            item.put("title", card.getMaskCardNum());
            item.put("description", WhatsAppUtils.formatCardDescription(card.getProductName(),
                card.getWalletInfo() != null ? card.getWalletInfo().getAvailBal() : "0"));
            listItems.add(item);
        }
        
        sendWhatsAppGenericInteractiveList(to, "💳 Card Selection", "Select a card for payment:", 
            "View Cards", "Your Cards", listItems, "card");
    }

    private void sendWhatsAppGenericInteractiveList(String to, String headerText, String bodyText, 
            String buttonText, String sectionTitle, List<Map<String, String>> items, String fallbackType) {
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
            body.put("text", bodyText);
            interactive.put("body", body);

            // Header (optional)
            Map<String, String> header = new HashMap<>();
            header.put("type", "text");
            header.put("text", headerText);
            interactive.put("header", header);

            // Action with list
            Map<String, Object> action = new HashMap<>();
            action.put("button", buttonText);

            // List sections
            List<Map<String, Object>> sections = new ArrayList<>();
            Map<String, Object> section = new HashMap<>();
            section.put("title", sectionTitle);

            // List rows
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Map<String, String> item : items) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", item.get("id"));
                row.put("title", item.get("title"));
                row.put("description", item.get("description"));
                rows.add(row);
            }

            section.put("rows", rows);
            sections.add(section);
            action.put("sections", sections);

            interactive.put("action", action);
            messagePayload.put("interactive", interactive);

            log.info("Generic interactive list payload created for {}", to);
            log.debug("Payload: {}", messagePayload);

            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            // Create HTTP entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(messagePayload, headers);

            log.info("Sending generic interactive list to WhatsApp API...");

            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(whatsappApiUrl, HttpMethod.POST, requestEntity, String.class);

            log.info("WhatsApp API response status: {}", response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Generic interactive list sent successfully to {}", to);
            } else {
                log.error("Failed to send generic interactive list to {}. Status: {}, Response: {}", to, response.getStatusCode(), response.getBody());
                throw new RuntimeException("WhatsApp API rejected interactive list");
            }

        } catch (Exception e) {
            log.error("Error sending WhatsApp generic interactive list to {}: {}", to, e.getMessage(), e);
            // Fallback to regular text message
            sendFallbackTextMessage(to, items, fallbackType);
        }
    }

    private void sendFallbackTextMessage(String to, List<Map<String, String>> items, String type) {
        StringBuilder fallbackMenu = new StringBuilder();
        
        if ("merchant".equals(type)) {
            fallbackMenu.append("Hi, welcome to AppoPay\n\nSelect Merchant to Pay:\n\n");
        } else if ("card".equals(type)) {
            fallbackMenu.append("Select a card for payment:\n\n");
        }
        
        for (int i = 0; i < items.size(); i++) {
            fallbackMenu.append(i + 1).append(") ").append(items.get(i).get("title")).append("\n");
            if (items.get(i).get("description") != null && !items.get(i).get("description").isEmpty()) {
                fallbackMenu.append("   ").append(items.get(i).get("description")).append("\n");
            }
            fallbackMenu.append("\n");
        }
        
        sendWhatsAppMessage(to, fallbackMenu.toString().trim());
    }

    private String formatWhatsAppNumberForValidation(String whatsappNumber) {
        return "+" + whatsappNumber;
    }

    private boolean purchaseTrx(UserSession session){
        try {
            log.info("Initiating purchase transaction for session: {}", session.getPhoneNumber());
            
            PurchaseTrxRequest request = new PurchaseTrxRequest();

            // Override specific fields from UserSession
            request.getRequestData().getIsoReqData().setFld11(WhatsAppUtils.generateUnique6CharString());
            request.getRequestData().getIsoReqData().setFld12(WhatsAppUtils.getCurrentTimeHHMMSS());
            request.getRequestData().getIsoReqData().setFld13(WhatsAppUtils.getCurrentDateMMYY());
            request.getRequestData().getIsoReqData().setFld4(WhatsAppUtils.convertToISO8583Amount(String.valueOf(session.getAmount())));
            request.getRequestData().getIsoReqData().setFld2(merchantService.decryptCmsCardNumber(session.getCardNo()).getRespInfo().getRespData().getDCardNum());
            log.debug("Purchase request prepared with card: {}, CVV: {}, PIN: {}", 
                session.getCardNo(), "***", "******");
            
            // Print the complete request body before sending
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String requestJson = objectMapper.writeValueAsString(request);
                log.info("=== PURCHASE REQUEST BODY ===");
                log.info("{}", requestJson);
                log.info("=== END REQUEST BODY ===");
            } catch (Exception e) {
                log.warn("Failed to serialize request body for logging: {}", e.getMessage());
            }
            
            // Call merchant service
            PurchaseTrxResponse response = merchantService.purchaseTrx(request);
            
            if (response != null && response.getRespInfo() != null) {
                String respCode = response.getRespInfo().getRespCode();
                String respDesc = response.getRespInfo().getRespDesc();
                
                log.info("Purchase transaction response - Code: {}, Desc: {}", respCode, respDesc);
                
                // Check if transaction was successful (assuming "0" means success)
                if ("0".equals(respCode)) {
                    log.info("Purchase transaction successful for session: {}", session.getPhoneNumber());
                    return true;
                } else {
                    log.warn("Purchase transaction failed - Code: {}, Desc: {}", respCode, respDesc);
                    return false;
                }
            } else {
                log.error("Invalid response received from purchase transaction");
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error during purchase transaction for session {}: {}", 
                session.getPhoneNumber(), e.getMessage(), e);
            return false;
        }
    }


    private void sendWhatsAppDocument(String to, String pdfId, String filename, String caption) {
        try {
            // Generate URL to serve PDF from our own server
            String documentUrl = serverBaseUrl + "/api/pdf/" + pdfId;
            
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("messaging_product", "whatsapp");
            messagePayload.put("to", to);
            messagePayload.put("type", "document");
            
            Map<String, Object> document = new HashMap<>();
            document.put("link", documentUrl);
            document.put("filename", filename);
            document.put("caption", caption);
            
            messagePayload.put("document", document);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(messagePayload, headers);
            ResponseEntity<String> response = restTemplate.exchange(whatsappApiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("WhatsApp document sent successfully to {}", to);
                
                // Schedule PDF cleanup after 5 minutes (WhatsApp should have downloaded it by then)
                new Thread(() -> {
                    try {
                        Thread.sleep(5 * 60 * 1000); // 5 minutes
                        pdfService.removePdf(pdfId);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.warn("PDF cleanup interrupted for ID: {}", pdfId);
                    }
                }).start();
                
            } else {
                log.error("Failed to send WhatsApp document to {}: {}", to, response.getBody());
                // Clean up immediately if sending failed
                pdfService.removePdf(pdfId);
            }

        } catch (Exception e) {
            log.error("Error sending WhatsApp document to {}: {}", to, e.getMessage(), e);
        }
    }

}
