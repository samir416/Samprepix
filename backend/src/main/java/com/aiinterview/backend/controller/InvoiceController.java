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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class InvoiceController {

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

        Invoice invoice = invoiceRepository.findById(invoiceId)
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

        Invoice invoice = invoiceRepository.findById(invoiceId)
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
            throw new RuntimeException(
                    "Unable to generate invoice PDF"
            );
        }
    }

    private Map<String, Object> buildInvoiceResponse(Invoice invoice) {
        Map<String, Object> response = new LinkedHashMap<>();

        Payment payment = invoice.getPayment();

        response.put("id", invoice.getId());
        response.put("invoiceNumber", invoice.getInvoiceNumber());
        response.put(
                "plan",
                invoice.getPlan() != null
                        ? safe(invoice.getPlan().getName())
                        : "N/A"
        );
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

                content.setNonStrokingColor(20, 28, 48);
                content.beginText();
                content.setFont(bold, 25);
                content.newLineAtOffset(
                        margin,
                        pageHeight - 68
                );
                content.showText("SAMPREPIX");
                content.endText();

                content.setNonStrokingColor(100, 110, 125);
                content.beginText();
                content.setFont(regular, 9);
                content.newLineAtOffset(
                        margin,
                        pageHeight - 86
                );
                content.showText(
                        "AI Placement & Interview Preparation Platform"
                );
                content.endText();

                content.setNonStrokingColor(40, 80, 180);
                content.addRect(
                        pageWidth - margin - 122,
                        pageHeight - 92,
                        122,
                        44
                );
                content.fill();

                content.setNonStrokingColor(255, 255, 255);
                content.beginText();
                content.setFont(bold, 15);
                content.newLineAtOffset(
                        pageWidth - margin - 94,
                        pageHeight - 67
                );
                content.showText("INVOICE");
                content.endText();

                float y = pageHeight - 132;

                content.setStrokingColor(220, 225, 232);
                content.moveTo(margin, y);
                content.lineTo(pageWidth - margin, y);
                content.stroke();

                y -= 34;

                drawLabelValue(
                        content,
                        bold,
                        regular,
                        "Invoice Number",
                        safe(invoice.getInvoiceNumber()),
                        margin,
                        y,
                        margin + 105
                );

                drawLabelValue(
                        content,
                        bold,
                        regular,
                        "Issued",
                        invoice.getIssuedAt() != null
                                ? invoice.getIssuedAt()
                                .format(DATE_FORMAT)
                                : "N/A",
                        pageWidth / 2,
                        y,
                        pageWidth / 2 + 45
                );

                y -= 55;

                content.setNonStrokingColor(40, 48, 65);
                content.beginText();
                content.setFont(bold, 12);
                content.newLineAtOffset(margin, y);
                content.showText("Billed To");
                content.endText();

                y -= 20;

                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(margin, y);
                content.showText(safe(user.getName()));
                content.endText();

                y -= 16;

                content.setFont(regular, 10);
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText(safe(user.getEmail()));
                content.endText();

                y -= 45;

                content.setNonStrokingColor(246, 248, 252);
                content.addRect(
                        margin,
                        y - 70,
                        pageWidth - margin * 2,
                        70
                );
                content.fill();

                content.setNonStrokingColor(45, 54, 72);

                content.beginText();
                content.setFont(bold, 9);
                content.newLineAtOffset(
                        margin + 14,
                        y - 19
                );
                content.showText("PLAN");
                content.endText();

                content.beginText();
                content.setFont(bold, 9);
                content.newLineAtOffset(
                        margin + 205,
                        y - 19
                );
                content.showText("STATUS");
                content.endText();

                content.beginText();
                content.setFont(bold, 9);
                content.newLineAtOffset(
                        pageWidth - margin - 125,
                        y - 19
                );
                content.showText("TOTAL");
                content.endText();

                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(
                        margin + 14,
                        y - 43
                );
                content.showText(
                        invoice.getPlan() != null
                                ? safe(invoice.getPlan().getName())
                                : "N/A"
                );
                content.endText();

                content.setNonStrokingColor(30, 130, 80);
                content.beginText();
                content.setFont(bold, 10);
                content.newLineAtOffset(
                        margin + 205,
                        y - 43
                );
                content.showText(safe(invoice.getStatus()));
                content.endText();

                content.setNonStrokingColor(20, 28, 48);
                content.beginText();
                content.setFont(bold, 12);
                content.newLineAtOffset(
                        pageWidth - margin - 125,
                        y - 43
                );
                content.showText(
                        formatMoney(
                                invoice.getAmount(),
                                invoice.getCurrency()
                        )
                );
                content.endText();

                y -= 105;

                content.setNonStrokingColor(40, 48, 65);
                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(margin, y);
                content.showText("Payment Details");
                content.endText();

                y -= 24;

                Payment payment = invoice.getPayment();

                drawDetail(
                        content,
                        regular,
                        "Payment Gateway",
                        "Cashfree",
                        margin,
                        y
                );

                y -= 18;

                drawDetail(
                        content,
                        regular,
                        "Payment Method",
                        payment != null
                                ? safe(payment.getPaymentMethod())
                                : "N/A",
                        margin,
                        y
                );

                y -= 18;

                drawDetail(
                        content,
                        regular,
                        "Cashfree Order ID",
                        safe(invoice.getCashfreeOrderId()),
                        margin,
                        y
                );

                y -= 18;

                drawDetail(
                        content,
                        regular,
                        "Payment Reference ID",
                        payment != null
                                ? safe(payment.getCashfreePaymentId())
                                : "N/A",
                        margin,
                        y
                );

                y -= 18;

                drawDetail(
                        content,
                        regular,
                        "Currency",
                        safe(invoice.getCurrency()),
                        margin,
                        y
                );

                y -= 18;

                drawDetail(
                        content,
                        regular,
                        "Payment Status",
                        safe(invoice.getStatus()),
                        margin,
                        y
                );

                y -= 42;

                content.setStrokingColor(220, 225, 232);
                content.moveTo(margin, y);
                content.lineTo(pageWidth - margin, y);
                content.stroke();

                y -= 30;

                content.setNonStrokingColor(20, 28, 48);
                content.beginText();
                content.setFont(bold, 11);
                content.newLineAtOffset(margin, y);
                content.showText("Order Summary");
                content.endText();

                y -= 23;

                drawSummaryRow(
                        content,
                        regular,
                        bold,
                        "Plan",
                        invoice.getPlan() != null
                                ? safe(invoice.getPlan().getName())
                                : "N/A",
                        margin,
                        pageWidth - margin,
                        y
                );

                y -= 20;

                drawSummaryRow(
                        content,
                        regular,
                        bold,
                        "Subtotal",
                        formatMoney(
                                invoice.getAmount(),
                                invoice.getCurrency()
                        ),
                        margin,
                        pageWidth - margin,
                        y
                );

                y -= 20;

                drawSummaryRow(
                        content,
                        regular,
                        bold,
                        "Total Paid",
                        formatMoney(
                                invoice.getAmount(),
                                invoice.getCurrency()
                        ),
                        margin,
                        pageWidth - margin,
                        y
                );

                y -= 45;

                content.setNonStrokingColor(40, 80, 180);
                content.addRect(
                        margin,
                        y - 46,
                        pageWidth - margin * 2,
                        46
                );
                content.fill();

                content.setNonStrokingColor(255, 255, 255);
                content.beginText();
                content.setFont(bold, 10);
                content.newLineAtOffset(
                        margin + 15,
                        y - 19
                );
                content.showText(
                        "Payment processed securely through Cashfree"
                );
                content.endText();

                content.beginText();
                content.setFont(regular, 8);
                content.newLineAtOffset(
                        margin + 15,
                        y - 34
                );
                content.showText(
                        "This invoice confirms the transaction recorded by Samprepix."
                );
                content.endText();

                y -= 82;

                content.setStrokingColor(220, 225, 232);
                content.moveTo(margin, y);
                content.lineTo(pageWidth - margin, y);
                content.stroke();

                y -= 27;

                content.setNonStrokingColor(40, 48, 65);
                content.beginText();
                content.setFont(bold, 10);
                content.newLineAtOffset(margin, y);
                content.showText(
                        "Thank you for choosing Samprepix."
                );
                content.endText();

                y -= 17;

                content.setNonStrokingColor(105, 115, 130);
                content.beginText();
                content.setFont(regular, 8);
                content.newLineAtOffset(margin, y);
                content.showText(
                        "This invoice is electronically generated and does not require a signature."
                );
                content.endText();

                y -= 14;

                content.beginText();
                content.setFont(regular, 8);
                content.newLineAtOffset(margin, y);
                content.showText(
                        "Samprepix • AI-powered placement preparation"
                );
                content.endText();
            }

            document.save(output);
            return output.toByteArray();
        }
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

        content.setNonStrokingColor(45, 54, 72);
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

        content.setNonStrokingColor(80, 90, 105);
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

        content.setNonStrokingColor(70, 80, 95);
        content.beginText();
        content.setFont(regular, 9);
        content.newLineAtOffset(left, y);
        content.showText(label);
        content.endText();

        content.setNonStrokingColor(30, 38, 52);
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
                .trim();

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