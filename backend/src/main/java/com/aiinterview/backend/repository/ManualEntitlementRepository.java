package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.ManualEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualEntitlementRepository extends JpaRepository<ManualEntitlement, Long> {
    List<ManualEntitlement> findByUserIdAndRevokedFalse(Long userId);
    List<ManualEntitlement> findByUserId(Long userId);
    boolean existsByUserIdAndPlanNameAndRevokedFalse(Long userId, String planName);
}
