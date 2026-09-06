package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.CodingProblemCompletion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT c FROM CodingProblemCompletion c JOIN FETCH c.problem WHERE c.user.id = :userId")
    List<CodingProblemCompletion> findAllByUserIdWithProblem(
            @Param("userId") Long userId
    );

    @Query("SELECT c FROM CodingProblemCompletion c JOIN FETCH c.problem WHERE c.user.id = :userId ORDER BY c.lastAttemptAt DESC")
    List<CodingProblemCompletion> findRecentByUserIdWithProblem(
            @Param("userId") Long userId,
            Pageable pageable
    );

    boolean existsByUserIdAndProblemIdAndCompletedTrue(
            Long userId,
            Long problemId
    );

    long countByUserId(
            Long userId
    );

    long countByUserIdAndCompletedTrue(
            Long userId
    );
}