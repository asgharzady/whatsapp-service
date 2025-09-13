package com.whatsapp.service.service;

import com.whatsapp.service.entity.UserSession;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    // In-memory storage for temporary PDFs
    private final Map<String, byte[]> pdfCache = new ConcurrentHashMap<>();

    public String generateReceiptPDF(UserSession session, String amount) {
        try {
            // Generate unique PDF ID
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String pdfId = "receipt_" + session.getPhoneNumber() + "_" + timestamp;

            // Create PDF document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Header - AppoPay
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 750);
            contentStream.showText("AppoPay");
            contentStream.endText();

            // Subtitle
            contentStream.setFont(PDType1Font.HELVETICA, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 720);
            contentStream.showText("Payment Gateway");
            contentStream.endText();

            // Receipt title
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 680);
            contentStream.showText("Payment Receipt");
            contentStream.endText();

            // Transaction details
            contentStream.setFont(PDType1Font.HELVETICA, 14);
            
            // Merchant
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 630);
            contentStream.showText("Merchant: " + (session.getSelectedMerchant() != null ? session.getSelectedMerchant() : "N/A"));
            contentStream.endText();

            // Amount
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 600);
            contentStream.showText("Amount: " + (amount != null ? amount : "N/A"));
            contentStream.endText();

            // Card (masked)
            String maskedCard = session.getCardNo() != null ? 
                "**** **** **** " + session.getCardNo().substring(Math.max(0, session.getCardNo().length() - 4)) : "N/A";
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 570);
            contentStream.showText("Card: " + maskedCard);
            contentStream.endText();

            // Date and time
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 540);
            contentStream.showText("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            contentStream.endText();

            // Transaction ID (using session phone + timestamp)
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 510);
            contentStream.showText("Transaction ID: TXN" + session.getPhoneNumber() + timestamp);
            contentStream.endText();

            // Status
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 470);
            contentStream.showText("Status: SUCCESSFUL");
            contentStream.endText();

            // Footer
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(100, 400);
            contentStream.showText("Thank you for using AppoPay!");
            contentStream.endText();

            contentStream.beginText();
            contentStream.newLineAtOffset(100, 380);
            contentStream.showText("For support, contact us at support@appopay.com");
            contentStream.endText();

            contentStream.close();
            
            // Save PDF to memory instead of file
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            document.close();
            
            byte[] pdfBytes = outputStream.toByteArray();
            pdfCache.put(pdfId, pdfBytes);
            
            log.info("PDF receipt generated successfully in memory with ID: {}", pdfId);
            return pdfId;

        } catch (Exception e) {
            log.error("Error generating PDF receipt: {}", e.getMessage(), e);
            return null;
        }
    }

    public byte[] getPdfBytes(String pdfId) {
        return pdfCache.get(pdfId);
    }

    public void removePdf(String pdfId) {
        pdfCache.remove(pdfId);
        log.info("PDF removed from cache: {}", pdfId);
    }

    public int getCacheSize() {
        return pdfCache.size();
    }
}
