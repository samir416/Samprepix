package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.repository.CodingProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@Transactional(readOnly = true)
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
    public List<CodingProblem> getProblems() {

        return codingProblemRepository
                .findByActiveTrue();
    }

    @Override
    public List<CodingProblem> getProblemsByDifficulty(
            String difficulty
    ) {

        if (
                difficulty == null ||
                difficulty.isBlank()
        ) {
            return List.of();
        }

        String normalizedDifficulty =
                difficulty
                        .trim()
                        .toUpperCase();

        if (
                !normalizedDifficulty.equals("EASY") &&
                !normalizedDifficulty.equals("MEDIUM") &&
                !normalizedDifficulty.equals("HARD")
        ) {
            return List.of();
        }

        return codingProblemRepository
                .findByDifficultyAndActiveTrue(
                        normalizedDifficulty
                );
    }

    @Override
    public List<CodingProblem> getProblemsForExperience(
            Integer experienceLevel
    ) {

        if (
                experienceLevel == null ||
                experienceLevel < 1
        ) {
            return List.of();
        }

        return codingProblemRepository
                .findByMinimumExperienceLevelLessThanEqualAndActiveTrue(
                        experienceLevel
                );
    }

    @Override
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

    @Override
    public Page<CodingProblem> searchProblems(
            String search,
            String difficulty,
            Pageable pageable
    ) {
        return searchProblems(search, difficulty, null, pageable);
    }

    @Override
    public Page<CodingProblem> searchProblems(
            String search,
            String difficulty,
            String tag,
            Pageable pageable
    ) {
        return searchProblems(search, difficulty, tag, null, pageable);
    }

    @Override
    public Page<CodingProblem> searchProblems(
            String search,
            String difficulty,
            String tag,
            String category,
            Pageable pageable
    ) {
        String normalizedSearch = search == null ? "" : search.trim();
        String normalizedDifficulty = difficulty == null
                ? ""
                : difficulty.trim().toUpperCase();
        String normalizedTag = tag == null ? "" : tag.trim();
        String normalizedCategory = category == null ? "" : category.trim().toUpperCase();

        if (!normalizedDifficulty.isBlank() &&
                !normalizedDifficulty.equals("EASY") &&
                !normalizedDifficulty.equals("MEDIUM") &&
                !normalizedDifficulty.equals("HARD")) {
            return Page.empty(pageable);
        }

        if (!normalizedCategory.isBlank()) {
            return codingProblemRepository.searchActiveProblemsFiltered(
                    normalizedCategory,
                    normalizedDifficulty.isBlank() ? null : normalizedDifficulty,
                    normalizedTag.isBlank() ? null : normalizedTag,
                    normalizedSearch.isBlank() ? null : normalizedSearch,
                    pageable
            );
        }

        if (normalizedTag.isBlank()) {
            if (normalizedDifficulty.isBlank() && normalizedSearch.isBlank()) {
                return codingProblemRepository.findByActiveTrue(pageable);
            }

            if (normalizedDifficulty.isBlank()) {
                return codingProblemRepository
                        .searchActiveProblems(
                                normalizedSearch,
                                pageable
                        );
            }

            if (normalizedSearch.isBlank()) {
                return codingProblemRepository.findByDifficultyAndActiveTrue(
                        normalizedDifficulty,
                        pageable
                );
            }

            return codingProblemRepository
                    .searchActiveProblemsByDifficulty(
                            normalizedDifficulty,
                            normalizedSearch,
                            pageable
                    );
        }

        return codingProblemRepository.searchActiveProblemsFiltered(
                null,
                normalizedDifficulty.isBlank() ? null : normalizedDifficulty,
                normalizedTag,
                normalizedSearch.isBlank() ? null : normalizedSearch,
                pageable
        );
    }

    @Override
    public List<String> getAvailableTags() {
        return codingProblemRepository.findDistinctActiveTags();
    }
}