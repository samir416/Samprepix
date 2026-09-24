package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.ManualEntitlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManualEntitlementRepository
        extends JpaRepository<ManualEntitlement, Long> {

    List<ManualEntitlement> findByUserIdAndRevokedFalse(
            Long userId
    );

    List<ManualEntitlement> findByUserId(
            Long userId
    );

    List<ManualEntitlement> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<ManualEntitlement> findByGrantStatusOrderByCreatedAtDesc(
            String grantStatus
    );

    List<ManualEntitlement> findByEmailStatusOrderByCreatedAtDesc(
            String emailStatus
    );

    Optional<ManualEntitlement> findTopByUserIdAndPlanNameAndRevokedFalseOrderByCreatedAtDesc(
            Long userId,
            String planName
    );

    boolean existsByUserIdAndPlanNameAndRevokedFalse(
            Long userId,
            String planName
    );

    boolean existsByUserIdAndPlanNameAndGrantStatusAndRevokedFalse(
            Long userId,
            String planName,
            String grantStatus
    );
}