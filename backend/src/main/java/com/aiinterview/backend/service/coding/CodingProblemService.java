package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;

import java.util.List;

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
}