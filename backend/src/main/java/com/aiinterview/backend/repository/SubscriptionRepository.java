package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.Subscription;
import com.aiinterview.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUserAndSubscriptionStatus(User user, String subscriptionStatus);
    List<Subscription> findByUserAndSubscriptionStatusOrderBySubscribedAtDesc(User user, String subscriptionStatus);
    List<Subscription> findByUserId(Long userId);
    Optional<Subscription> findByRazorpaySubscriptionId(String razorpaySubscriptionId);
    Optional<Subscription> findByRazorpayOrderId(String razorpayOrderId);
}
