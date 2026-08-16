package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.CodingProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CodingProblemRepository
        extends JpaRepository<CodingProblem, Long> {

    List<CodingProblem> findByActiveTrue();

    List<CodingProblem> findByDifficultyAndActiveTrue(
            String difficulty
    );

    List<CodingProblem> findByMinimumExperienceLevelLessThanEqualAndActiveTrue(
            Integer experienceLevel
    );

    Optional<CodingProblem> findByTitleIgnoreCase(
            String title
    );
}