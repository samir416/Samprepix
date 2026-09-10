package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.EntitlementHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntitlementHistoryRepository extends JpaRepository<EntitlementHistory, Long> {
    List<EntitlementHistory> findByUserId(Long userId);
}
