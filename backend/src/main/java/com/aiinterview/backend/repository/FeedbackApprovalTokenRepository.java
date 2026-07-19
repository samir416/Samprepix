package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.FeedbackApprovalToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackApprovalTokenRepository
        extends JpaRepository<FeedbackApprovalToken, Long> {

    /**
     * Find token by its unique value.
     */
    Optional<FeedbackApprovalToken> findByToken(String token);

    /**
     * Find token for a specific feedback.
     */
    Optional<FeedbackApprovalToken> findByFeedbackId(Long feedbackId);

    /**
     * Check whether a token already exists for feedback.
     */
    boolean existsByFeedbackId(Long feedbackId);

    /**
     * Delete token for a feedback.
     */
    void deleteByFeedbackId(Long feedbackId);
}