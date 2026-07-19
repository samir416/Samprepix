package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.FeedbackStatus;
import com.aiinterview.backend.entity.InterviewFeedback;
import com.aiinterview.backend.entity.InterviewSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewFeedbackRepository
        extends JpaRepository<InterviewFeedback, Long> {

    /**
     * Find feedback by interview session.
     */
    Optional<InterviewFeedback> findByInterviewSession(
            InterviewSession interviewSession
    );

    /**
     * Check whether feedback already exists
     * for the given interview session.
     */
    boolean existsByInterviewSession(
            InterviewSession interviewSession
    );

    /**
     * Find feedback by moderation status.
     */
    List<InterviewFeedback> findByStatus(
            FeedbackStatus status
    );

    /**
     * Public feedback (APPROVED only).
     */
    Page<InterviewFeedback> findByStatusOrderByCreatedAtDesc(
            FeedbackStatus status,
            Pageable pageable
    );

    /**
     * Count feedback by status.
     */
    long countByStatus(
            FeedbackStatus status
    );

}