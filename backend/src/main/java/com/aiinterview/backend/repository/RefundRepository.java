package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.Refund;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByRefundId(String refundId);

    Optional<Refund> findByCashfreeOrderId(String cashfreeOrderId);

    Optional<Refund> findByPaymentId(Long paymentId);

    Optional<Refund> findBySubscriptionId(Long subscriptionId);

    List<Refund> findByUserOrderByCreatedAtDesc(User user);

    List<Refund> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Refund> findByRefundStatusOrderByCreatedAtDesc(String refundStatus);

    boolean existsByRefundId(String refundId);

    boolean existsByPaymentId(Long paymentId);

    boolean existsBySubscriptionId(Long subscriptionId);
}