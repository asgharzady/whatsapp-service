package com.whatsapp.service.controller;

import com.whatsapp.service.service.WhatsAppWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("whatsapp/")
public class WhatsappController {
    private static final Logger log = LoggerFactory.getLogger(WhatsappController.class);

    private final WhatsAppWebhookService webhookService;

    public WhatsappController(WhatsAppWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    // GET webhook verification
    @GetMapping("webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(name = "hub.challenge", required = false) String challenge
    ) {
        System.out.println("got reqqqqq");
        return webhookService.verifyWebhook(mode, verifyToken, challenge);
    }

    @PostMapping("webhook")
    public ResponseEntity<String> handleIncomingWebhook(@RequestBody Map<String, Object> payload) {
        // Print complete incoming JSON payload
        System.out.println("📩 Incoming WhatsApp Webhook Payload: " + payload);

        // Call the service method
        webhookService.processIncomingMessage(payload);

        // Respond to Meta to confirm receipt
        return ResponseEntity.ok("EVENT_RECEIVED");
    }



}
