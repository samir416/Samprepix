package com.aiinterview.backend.repository;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodingTestCaseRepository
        extends JpaRepository<CodingTestCase, Long> {

    List<CodingTestCase> findByProblemAndActiveTrueOrderByTestCaseNumberAsc(
            CodingProblem problem
    );

    List<CodingTestCase> findByProblemAndHiddenFalseAndActiveTrueOrderByTestCaseNumberAsc(
            CodingProblem problem
    );

    List<CodingTestCase> findByProblemAndHiddenTrueAndActiveTrueOrderByTestCaseNumberAsc(
            CodingProblem problem
    );
}