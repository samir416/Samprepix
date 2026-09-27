package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Invoice> findByCashfreeOrderId(String cashfreeOrderId);

    boolean existsByInvoiceNumber(String invoiceNumber);

    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.plan LEFT JOIN FETCH i.payment WHERE i.user.id = :userId ORDER BY i.createdAt DESC")
    List<Invoice> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    List<Invoice> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            String status
    );

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.plan LEFT JOIN FETCH i.payment WHERE i.user.id = :userId AND i.invoiceNumber = :invoiceNumber")
    Optional<Invoice> findByUserIdAndInvoiceNumber(
            @Param("userId") Long userId,
            @Param("invoiceNumber") String invoiceNumber
    );

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.plan LEFT JOIN FETCH i.payment WHERE i.id = :id")
    Optional<Invoice> findByIdWithDetails(@Param("id") Long id);
}