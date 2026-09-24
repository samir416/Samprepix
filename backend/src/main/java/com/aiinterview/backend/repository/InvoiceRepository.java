package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Invoice> findByCashfreeOrderId(String cashfreeOrderId);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Invoice> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            String status
    );

    Optional<Invoice> findByUserIdAndInvoiceNumber(
            Long userId,
            String invoiceNumber
    );
}