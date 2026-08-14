package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.repository.CodingProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CodingProblemServiceImpl
        implements CodingProblemService {

    private final CodingProblemRepository codingProblemRepository;

    public CodingProblemServiceImpl(
            CodingProblemRepository codingProblemRepository
    ) {
        this.codingProblemRepository =
                codingProblemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingProblem> getProblems() {

        return codingProblemRepository
                .findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingProblem> getProblemsByDifficulty(
            String difficulty
    ) {

        if (difficulty == null ||
                difficulty.isBlank()) {

            return List.of();
        }

        return codingProblemRepository
                .findByDifficultyAndActiveTrue(
                        difficulty.trim().toUpperCase()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingProblem> getProblemsForExperience(
            Integer experienceLevel
    ) {

        if (experienceLevel == null ||
                experienceLevel < 1) {

            return List.of();
        }

        return codingProblemRepository
                .findByMinimumExperienceLevelLessThanEqualAndActiveTrue(
                        experienceLevel
                );
    }

    @Override
    @Transactional(readOnly = true)
    public CodingProblem getProblemById(
            Long id
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Problem ID cannot be null."
            );
        }

        return codingProblemRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Coding problem not found."
                        )
                );
    }
}