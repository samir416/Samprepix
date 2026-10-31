package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.UpgradeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UpgradeTransactionRepository extends JpaRepository<UpgradeTransaction, Long> {

    Optional<UpgradeTransaction> findByCashfreeOrderId(String cashfreeOrderId);

    List<UpgradeTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UpgradeTransaction> findBySubscriptionIdOrderByCreatedAtDesc(Long subscriptionId);
}
