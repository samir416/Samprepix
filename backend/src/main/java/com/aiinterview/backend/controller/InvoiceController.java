package com.aiinterview.backend.controller;

import com.aiinterview.backend.entity.Invoice;
import com.aiinterview.backend.entity.Payment;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.InvoiceRepository;
import com.aiinterview.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class InvoiceController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InvoiceController.class);

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    @GetMapping
    public ResponseEntity<?> getMyInvoices(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        List<Invoice> invoices =
                invoiceRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        List<Map<String, Object>> response = new ArrayList<>();

        for (Invoice invoice : invoices) {
            response.add(buildInvoiceResponse(invoice));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{invoiceId}")
    public ResponseEntity<?> getInvoice(
            @PathVariable Long invoiceId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        validateInvoiceId(invoiceId);

        Invoice invoice = invoiceRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        validateOwnership(invoice, user);

        return ResponseEntity.ok(buildInvoiceResponse(invoice));
    }

    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(
            @PathVariable Long invoiceId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);
        validateInvoiceId(invoiceId);

        Invoice invoice = invoiceRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));

        validateOwnership(invoice, user);

        try {
            byte[] pdf = generateInvoicePdf(invoice, user);

            String filename = "Samprepix-Invoice-"
                    + safe(invoice.getInvoiceNumber())
                    + ".pdf";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(filename)
                            .build()
            );
            headers.setContentLength(pdf.length);
            headers.setCacheControl("no-store, no-cache, must-revalidate");
            headers.setPragma("no-cache");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);

        } catch (Exception ex) {
            log.error("Failed to generate invoice PDF for invoiceId {}: {}", invoiceId, ex.getMessage(), ex);
            throw new RuntimeException("Unable to generate invoice PDF: " + ex.getMessage());
        }
    }

    private Map<String, Object> buildInvoiceResponse(Invoice invoice) {
        Map<String, Object> response = new LinkedHashMap<>();

        Payment payment = null;
        try {
            payment = invoice.getPayment();
        } catch (Exception ignored) {}

        String planName = "N/A";
        try {
            if (invoice.getPlan() != null) {
                planName = safe(invoice.getPlan().getName());
            }
        } catch (Exception ignored) {}

        response.put("id", invoice.getId());
        response.put("invoiceNumber", invoice.getInvoiceNumber());
        response.put("plan", planName);
        response.put("amount", invoice.getAmount());
        response.put("currency", safe(invoice.getCurrency()));
        response.put("status", safe(invoice.getStatus()));
        response.put("cashfreeOrderId", safe(invoice.getCashfreeOrderId()));
        response.put(
                "cashfreePaymentId",
                payment != null
                        ? safe(payment.getCashfreePaymentId())
                        : null
        );
        response.put(
                "paymentMethod",
                payment != null
                        ? safe(payment.getPaymentMethod())
                        : null
        );
        response.put("issuedAt", invoice.getIssuedAt());
        response.put("dueDate", invoice.getDueDate());
        response.put("createdAt", invoice.getCreatedAt());
        response.put("updatedAt", invoice.getUpdatedAt());

        return response;
    }

    private byte[] generateInvoicePdf(
            Invoice invoice,
            User user) throws Exception {

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float margin = 48;

            PDType1Font regular =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    );

            PDType1Font bold =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA_BOLD
                    );

            try (
                    PDPageContentStream content =
                            new PDPageContentStream(document, page)
            ) {

                setFillRgb(content, 20, 28, 48);
                content.beginText();
                content.setFont(bold, 24);
                content.newLineAtOffset(margin, pageHeight - 65);
                content.showText("SAMPREPIX");
                content.endText();

                setFillRgb(content, 100, 110, 125);
                content.beginText();
                content.setFont(regular, 9);
                content.newLineAtOffset(margin, pageHeight - 81);
                content.showText("AI Interview & Placement Preparation Platform");
                content.endText();

                setFillRgb(content, 37, 99, 235);
                content.addRect(pageWidth - margin - 150, pageHeight - 88, 150, 40);
                content.fill();

                setFillRgb(content, 255, 255, 255);
                content.beginText();
                content.setFont(bold, 13);
                content.newLineAtOffset(pageWidth - margin - 138, pageHeight - 65);
                content.showText("TAX INVOICE / RECEIPT");
                content.endText();

                float y = pageHeight - 118;

                setStrokeRgb(content, 220, 225, 232);
                content.moveTo(margin, y);
                content.lineTo(pageWidth - margin, y);
                content.stroke();

                y -= 26;

                drawLabelValue(content, bold, regular, "Invoice Number:", safe(invoice.getInvoiceNumber()), margin, y, margin + 95);
                drawLabelValue(content, bold, regular, "Payment Status:", safe(invoice.getStatus()).toUpperCase(), pageWidth / 2 + 10, y, pageWidth / 2 + 105);

                y -= 18;

                drawLabelValue(content, bold, regular, "Issue Date:", invoice.getIssuedAt() != null ? invoice.getIssuedAt().format(DATE_FORMAT) : "N/A", margin, y, margin + 95);
                drawLabelValue(content, bold, regular, "Payment Method:", "Cashfree PG", pageWidth / 2 + 10, y, pageWidth / 2 + 105);

                y -= 38;

                setFillRgb(content, 30, 41, 59);
                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(margin, y);
                content.showText("Billed To:");
                content.endText();

                y -= 16;
                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(margin, y);
                content.showText(safe(user.getName()));
                content.endText();

                y -= 15;
                content.beginText();
                content.setFont(regular, 10);
                content.newLineAtOffset(margin, y);
                content.showText(safe(user.getEmail()));
                content.endText();

                String userPhone = null;
                try {
                    if (user.getProfile() != null && user.getProfile().getPhone() != null && !user.getProfile().getPhone().isBlank()) {
                        userPhone = user.getProfile().getPhone().trim();
                    }
                } catch (Exception ignored) {}

                if (userPhone != null) {
                    y -= 15;
                    content.beginText();
                    content.setFont(regular, 10);
                    content.newLineAtOffset(margin, y);
                    content.showText("Phone: +91 " + userPhone);
                    content.endText();
                }

                y -= 32;

                // Itemized Table Header
                setFillRgb(content, 241, 245, 249);
                content.addRect(margin, y - 24, pageWidth - margin * 2, 24);
                content.fill();

                setFillRgb(content, 51, 65, 85);
                content.beginText();
                content.setFont(bold, 9);
                content.newLineAtOffset(margin + 10, y - 16);
                content.showText("ITEM / DESCRIPTION");
                content.endText();

                content.beginText();
                content.setFont(bold, 9);
                content.newLineAtOffset(margin + 200, y - 16);
                content.showText("PERIOD");
                content.endText();

                content.beginText();
                content.setFont(bold, 9);
                content.newLineAtOffset(margin + 295, y - 16);
                content.showText("QTY");
                content.endText();

                content.beginText();
                content.setFont(bold, 9);
                content.newLineAtOffset(margin + 350, y - 16);
                content.showText("RATE");
                content.endText();

                content.beginText();
                content.setFont(bold, 9);
                content.newLineAtOffset(pageWidth - margin - 70, y - 16);
                content.showText("AMOUNT");
                content.endText();

                y -= 32;

                // Item Row
                String planTitle = (invoice.getPlan() != null ? safe(invoice.getPlan().getName()) : "PRO") + " Plan - Monthly Access";
                String priceFormatted = formatMoney(invoice.getAmount(), invoice.getCurrency());

                setFillRgb(content, 15, 23, 42);
                content.beginText();
                content.setFont(bold, 10);
                content.newLineAtOffset(margin + 10, y);
                content.showText(planTitle);
                content.endText();

                content.beginText();
                content.setFont(regular, 9);
                content.newLineAtOffset(margin + 200, y);
                content.showText("30 Days");
                content.endText();

                content.beginText();
                content.setFont(regular, 9);
                content.newLineAtOffset(margin + 302, y);
                content.showText("1");
                content.endText();

                content.beginText();
                content.setFont(regular, 9);
                content.newLineAtOffset(margin + 350, y);
                content.showText(priceFormatted);
                content.endText();

                content.beginText();
                content.setFont(bold, 10);
                content.newLineAtOffset(pageWidth - margin - 70, y);
                content.showText(priceFormatted);
                content.endText();

                y -= 14;
                setStrokeRgb(content, 226, 232, 240);
                content.moveTo(margin, y);
                content.lineTo(pageWidth - margin, y);
                content.stroke();

                y -= 30;

                // Order Totals Summary
                drawSummaryRow(content, regular, bold, "Subtotal", priceFormatted, pageWidth / 2 + 20, pageWidth - margin, y);
                y -= 18;
                drawSummaryRow(content, regular, bold, "Estimated Taxes", "Included (₹0.00)", pageWidth / 2 + 20, pageWidth - margin, y);
                y -= 18;
                drawSummaryRow(content, regular, bold, "Total Paid", priceFormatted, pageWidth / 2 + 20, pageWidth - margin, y);

                y -= 35;

                // Payment and Subscription Details Box
                setFillRgb(content, 248, 250, 252);
                content.addRect(margin, y - 76, pageWidth - margin * 2, 76);
                content.fill();

                setStrokeRgb(content, 226, 232, 240);
                content.addRect(margin, y - 76, pageWidth - margin * 2, 76);
                content.stroke();

                setFillRgb(content, 30, 41, 59);
                content.beginText();
                content.setFont(bold, 10);
                content.newLineAtOffset(margin + 12, y - 18);
                content.showText("Transaction & Subscription Details");
                content.endText();

                Payment payment = invoice.getPayment();
                String paymentRef = (payment != null && payment.getCashfreePaymentId() != null)
                        ? payment.getCashfreePaymentId()
                        : "N/A";

                String validUntilStr = invoice.getDueDate() != null
                        ? invoice.getDueDate().format(DATE_FORMAT)
                        : (invoice.getIssuedAt() != null ? invoice.getIssuedAt().plusMonths(1).format(DATE_FORMAT) : "30 Days from issue");

                drawDetail(content, regular, "Cashfree Order ID", safe(invoice.getCashfreeOrderId()), margin + 12, y - 36);
                drawDetail(content, regular, "Payment Reference", paymentRef, margin + 12, y - 52);
                drawDetail(content, regular, "Subscription Valid Until", validUntilStr, margin + 12, y - 68);

                y -= 110;

                setStrokeRgb(content, 226, 232, 240);
                content.moveTo(margin, y);
                content.lineTo(pageWidth - margin, y);
                content.stroke();

                y -= 22;

                setFillRgb(content, 71, 85, 105);
                content.beginText();
                content.setFont(bold, 9);
                content.newLineAtOffset(margin, y);
                content.showText("Support: support@samprepix.com  |  Website: https://samprepix.com");
                content.endText();

                y -= 15;

                content.beginText();
                content.setFont(regular, 8);
                content.newLineAtOffset(margin, y);
                content.showText("This is an electronically generated tax invoice and does not require a physical signature.");
                content.endText();

                y -= 14;

                content.beginText();
                content.setFont(regular, 8);
                content.newLineAtOffset(margin, y);
                content.showText("Thank you for choosing SamPrepIX - Your AI Interview & Placement Preparation Partner.");
                content.endText();
            }

            document.save(output);
            return output.toByteArray();
        }
    }

    private static void setFillRgb(PDPageContentStream stream, int r, int g, int b) throws IOException {
        stream.setNonStrokingColor(r / 255.0f, g / 255.0f, b / 255.0f);
    }

    private static void setStrokeRgb(PDPageContentStream stream, int r, int g, int b) throws IOException {
        stream.setStrokingColor(r / 255.0f, g / 255.0f, b / 255.0f);
    }

    private void drawLabelValue(
            PDPageContentStream content,
            PDType1Font bold,
            PDType1Font regular,
            String label,
            String value,
            float labelX,
            float y,
            float valueX) throws Exception {

        setFillRgb(content, 45, 54, 72);
        content.beginText();
        content.setFont(bold, 9);
        content.newLineAtOffset(labelX, y);
        content.showText(label);
        content.endText();

        content.beginText();
        content.setFont(regular, 9);
        content.newLineAtOffset(valueX, y);
        content.showText(value);
        content.endText();
    }

    private void drawDetail(
            PDPageContentStream content,
            PDType1Font regular,
            String label,
            String value,
            float x,
            float y) throws Exception {

        setFillRgb(content, 80, 90, 105);
        content.beginText();
        content.setFont(regular, 9);
        content.newLineAtOffset(x, y);
        content.showText(label + ": " + value);
        content.endText();
    }

    private void drawSummaryRow(
            PDPageContentStream content,
            PDType1Font regular,
            PDType1Font bold,
            String label,
            String value,
            float left,
            float right,
            float y) throws Exception {

        setFillRgb(content, 70, 80, 95);
        content.beginText();
        content.setFont(regular, 9);
        content.newLineAtOffset(left, y);
        content.showText(label);
        content.endText();

        setFillRgb(content, 30, 38, 52);
        content.beginText();
        content.setFont(bold, 9);
        content.newLineAtOffset(right - 125, y);
        content.showText(value);
        content.endText();
    }

    private void validateOwnership(
            Invoice invoice,
            User user) {

        if (invoice == null
                || user == null
                || invoice.getUser() == null
                || invoice.getUser().getId() == null
                || user.getId() == null
                || !invoice.getUser().getId().equals(user.getId())) {

            throw new SecurityException(
                    "Invoice does not belong to authenticated user"
            );
        }
    }

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {

            throw new SecurityException("Authentication required");
        }

        String email = authentication.getName().trim();

        if (email.length() > 254
                || email.contains("\r")
                || email.contains("\n")) {

            throw new SecurityException("Invalid authentication identity");
        }

        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new SecurityException("User not found")
                );
    }

    private void validateInvoiceId(Long invoiceId) {
        if (invoiceId == null || invoiceId <= 0) {
            throw new IllegalArgumentException("Invalid invoice ID");
        }
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }

        String sanitized = value
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\t", " ")
                .replace("•", "-")
                .replace("₹", "INR ")
                .trim();

        StringBuilder sb = new StringBuilder();
        for (char c : sanitized.toCharArray()) {
            if (c >= 32 && c <= 126) {
                sb.append(c);
            } else if (c == '•') {
                sb.append('-');
            } else {
                sb.append(' ');
            }
        }
        sanitized = sb.toString().trim();

        if (sanitized.isBlank()) {
            return "N/A";
        }

        if (sanitized.length() > 500) {
            return sanitized.substring(0, 500);
        }

        return sanitized;
    }

    private String formatMoney(
            double amount,
            String currency) {

        double safeAmount =
                Double.isFinite(amount) && amount >= 0
                        ? amount
                        : 0.0;

        if ("USD".equalsIgnoreCase(currency)) {
            return String.format("$%.2f", safeAmount);
        }

        return String.format("INR %.2f", safeAmount);
    }
}