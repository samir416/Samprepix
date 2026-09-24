package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.Payment;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByCashfreeOrderId(String cashfreeOrderId);

    Optional<Payment> findByCashfreePaymentId(String cashfreePaymentId);

    List<Payment> findByUserOrderByCreatedAtDesc(User user);

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Payment> findByUserIdAndPaymentStatusOrderByCreatedAtDesc(
            Long userId,
            String paymentStatus
    );

    List<Payment> findByUserIdAndPlanNameOrderByCreatedAtDesc(
            Long userId,
            String planName
    );

    Optional<Payment> findTopByUserIdAndPlanNameAndPaymentStatusOrderByCreatedAtDesc(
            Long userId,
            String planName,
            String paymentStatus
    );

    Optional<Payment> findByRefundId(String refundId);

    boolean existsByCashfreeOrderId(String cashfreeOrderId);

    boolean existsByCashfreePaymentId(String cashfreePaymentId);
}