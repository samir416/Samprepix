package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.CodingProblemCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodingProblemCompletionRepository
        extends JpaRepository<CodingProblemCompletion, Long> {

    Optional<CodingProblemCompletion> findByUserIdAndProblemId(
            Long userId,
            Long problemId
    );

    List<CodingProblemCompletion> findByUserIdOrderByCompletedAtAsc(
            Long userId
    );

    List<CodingProblemCompletion> findByUserIdAndCompletedTrueOrderByCompletedAtAsc(
            Long userId
    );

    boolean existsByUserIdAndProblemIdAndCompletedTrue(
            Long userId,
            Long problemId
    );

    long countByUserIdAndCompletedTrue(
            Long userId
    );
}