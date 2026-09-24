package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserAndSubscriptionStatus(
            User user,
            String subscriptionStatus
    );

    List<Subscription> findByUserAndSubscriptionStatusOrderBySubscribedAtDesc(
            User user,
            String subscriptionStatus
    );

    List<Subscription> findByUserId(Long userId);

    List<Subscription> findByUserIdOrderBySubscribedAtDesc(Long userId);

    Optional<Subscription> findByRazorpaySubscriptionId(
            String razorpaySubscriptionId
    );

    Optional<Subscription> findByRazorpayOrderId(
            String razorpayOrderId
    );

    Optional<Subscription> findByRazorpayPaymentId(
            String razorpayPaymentId
    );

    Optional<Subscription> findByCashfreeOrderId(
            String cashfreeOrderId
    );

    Optional<Subscription> findByCashfreePaymentId(
            String cashfreePaymentId
    );

    List<Subscription> findByUserIdAndSubscriptionStatusOrderBySubscribedAtDesc(
            Long userId,
            String subscriptionStatus
    );

    Optional<Subscription> findTopByUserIdAndSubscriptionStatusOrderBySubscribedAtDesc(
            Long userId,
            String subscriptionStatus
    );

    List<Subscription> findByUserIdAndLifetimeTrueOrderBySubscribedAtDesc(
            Long userId
    );

    Optional<Subscription> findTopByUserIdAndLifetimeTrueOrderBySubscribedAtDesc(
            Long userId
    );

    List<Subscription> findBySubscriptionStatusOrderBySubscribedAtDesc(
            String subscriptionStatus
    );

    Optional<Subscription> findByIdAndUserId(
            Long id,
            Long userId
    );

    boolean existsByCashfreeOrderId(
            String cashfreeOrderId
    );

    boolean existsByCashfreePaymentId(
            String cashfreePaymentId
    );

    List<Subscription> findByAutoRenewTrueAndSubscriptionStatusAndExpiresAtBefore(
            String subscriptionStatus,
            LocalDateTime expiresAt
    );
}