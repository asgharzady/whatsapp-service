package com.whatsapp.service.controller;

import com.whatsapp.service.service.PdfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    @Autowired
    private PdfService pdfService;

    @GetMapping("/{pdfId}")
    public ResponseEntity<byte[]> getPdf(@PathVariable String pdfId) {
        try {
            byte[] pdfBytes = pdfService.getPdfBytes(pdfId);
            
            if (pdfBytes == null) {
                log.warn("PDF not found for ID: {}", pdfId);
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "receipt.pdf");
            headers.setContentLength(pdfBytes.length);

            log.info("Serving PDF for ID: {}", pdfId);
            
            // Clean up PDF after serving (optional - you might want to keep it for a while)
            // pdfService.removePdf(pdfId);
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error serving PDF for ID {}: {}", pdfId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/cache/size")
    public ResponseEntity<String> getCacheSize() {
        int size = pdfService.getCacheSize();
        return ResponseEntity.ok("PDF cache size: " + size);
    }

    @DeleteMapping("/{pdfId}")
    public ResponseEntity<String> deletePdf(@PathVariable String pdfId) {
        pdfService.removePdf(pdfId);
        return ResponseEntity.ok("PDF deleted: " + pdfId);
    }
}
