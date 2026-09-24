package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.Invoice;
import com.aiinterview.backend.entity.Payment;
import com.aiinterview.backend.entity.Plan;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final String PAID = "PAID";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice createInvoice(
            User user,
            Plan plan,
            Payment payment,
            double amount,
            String currency) {

        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (plan == null) {
            throw new IllegalArgumentException("Plan is required");
        }

        if (payment == null) {
            throw new IllegalArgumentException("Payment is required");
        }

        if (!Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("Invalid invoice amount");
        }

        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency is required");
        }

        String normalizedCurrency = currency.trim().toUpperCase();

        if (!"INR".equals(normalizedCurrency)
                && !"USD".equals(normalizedCurrency)) {
            throw new IllegalArgumentException("Unsupported currency");
        }

        String cashfreeOrderId = payment.getCashfreeOrderId();

        if (cashfreeOrderId != null && !cashfreeOrderId.isBlank()) {
            Invoice existing =
                    invoiceRepository.findByCashfreeOrderId(cashfreeOrderId)
                            .orElse(null);

            if (existing != null) {
                if (!user.getId().equals(existing.getUser().getId())) {
                    throw new SecurityException("Invoice ownership validation failed");
                }

                return existing;
            }
        }

        LocalDateTime now = LocalDateTime.now();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateUniqueInvoiceNumber())
                .user(user)
                .plan(plan)
                .payment(payment)
                .amount(amount)
                .currency(normalizedCurrency)
                .status(PAID)
                .cashfreeOrderId(cashfreeOrderId)
                .razorpayOrderId(payment.getRazorpayOrderId())
                .issuedAt(now)
                .dueDate(now.plusMonths(1))
                .createdAt(now)
                .updatedAt(now)
                .build();

        return invoiceRepository.save(invoice);
    }

    private String generateUniqueInvoiceNumber() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String datePart = LocalDateTime.now().format(DATE_FORMAT);

            String uniquePart = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();

            String candidate = "INV-" + datePart + "-" + uniquePart;

            if (!invoiceRepository.existsByInvoiceNumber(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "Unable to generate unique invoice number"
        );
    }

    public List<Invoice> getInvoicesByUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        return invoiceRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Invoice getInvoiceByUser(
            Long userId,
            String invoiceNumber) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalArgumentException("Invoice number is required");
        }

        return invoiceRepository
                .findByUserIdAndInvoiceNumber(
                        userId,
                        invoiceNumber.trim()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException("Invoice not found"));
    }
}