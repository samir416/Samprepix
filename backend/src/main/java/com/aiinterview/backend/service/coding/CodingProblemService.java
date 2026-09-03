package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CodingProblemService {

    List<CodingProblem> getProblems();

    List<CodingProblem> getProblemsByDifficulty(
            String difficulty
    );

    List<CodingProblem> getProblemsForExperience(
            Integer experienceLevel
    );

    CodingProblem getProblemById(
            Long id
    );

    Page<CodingProblem> searchProblems(
            String search,
            String difficulty,
            Pageable pageable
    );
}