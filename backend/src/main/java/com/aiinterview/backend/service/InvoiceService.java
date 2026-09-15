package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.*;
import com.aiinterview.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    @Transactional
    public Invoice createInvoice(User user, Plan plan, Payment payment, double amount, String currency) {
        String invoiceNumber = generateUniqueInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .user(user)
                .plan(plan)
                .payment(payment)
                .amount(amount)
                .currency(currency)
                .status("PAID")
                .cashfreeOrderId(payment.getCashfreeOrderId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .issuedAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(30))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return invoiceRepository.save(invoice);
    }

    private String generateUniqueInvoiceNumber() {
        String prefix = "INV";
        String datePart = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String candidate = prefix + "-" + datePart + "-" + uniquePart;

        if (invoiceRepository.existsByInvoiceNumber(candidate)) {
            return generateUniqueInvoiceNumber();
        }
        return candidate;
    }

    public List<Invoice> getInvoicesByUser(Long userId) {
        return invoiceRepository.findByUserId(userId);
    }
}
