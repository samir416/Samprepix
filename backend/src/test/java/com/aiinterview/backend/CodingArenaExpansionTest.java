package com.aiinterview.backend;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import com.aiinterview.backend.service.coding.CodingProblemService;
import com.aiinterview.backend.service.coding.FunctionExecutionWrapperService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CodingArenaExpansionTest {

    @Autowired
    private CodingProblemRepository codingProblemRepository;

    @Autowired
    private CodingTestCaseRepository codingTestCaseRepository;

    @Autowired
    private CodingProblemService codingProblemService;

    @Autowired
    private FunctionExecutionWrapperService wrapperService;

    @Test
    @DisplayName("Verify Coding Arena database contains at least 5,000 problems")
    void testTotalProblemsCount() {
        long count = codingProblemRepository.count();
        System.out.printf("Current Coding Problem count in database: %d%n", count);
        assertTrue(count >= 5050, "Expected at least 5050 problems in DB, found: " + count);
    }

    @Test
    @DisplayName("Verify test cases count in database is at least 20,000")
    void testTotalTestCasesCount() {
        long count = codingTestCaseRepository.count();
        System.out.printf("Current Test Case count in database: %d%n", count);
        assertTrue(count >= 20200, "Expected at least 20200 test cases in DB, found: " + count);
    }

    @Test
    @DisplayName("Verify difficulty distribution in Coding Arena problems")
    void testDifficultyDistribution() {
        List<CodingProblem> easy = codingProblemRepository.findByDifficultyAndActiveTrue("EASY");
        List<CodingProblem> medium = codingProblemRepository.findByDifficultyAndActiveTrue("MEDIUM");
        List<CodingProblem> hard = codingProblemRepository.findByDifficultyAndActiveTrue("HARD");

        System.out.printf("Problems by difficulty -> EASY: %d, MEDIUM: %d, HARD: %d%n",
                easy.size(), medium.size(), hard.size());

        assertTrue(easy.size() >= 1000, "Expected at least 1000 EASY problems, got: " + easy.size());
        assertTrue(medium.size() >= 2000, "Expected at least 2000 MEDIUM problems, got: " + medium.size());
        assertTrue(hard.size() >= 800, "Expected at least 800 HARD problems, got: " + hard.size());
    }

    @Test
    @DisplayName("Verify pagination and tag filtering")
    void testPaginationAndFiltering() {
        Page<CodingProblem> page0 = codingProblemService.searchProblems(
                null, null, null, PageRequest.of(0, 50, Sort.by("id"))
        );
        assertEquals(50, page0.getContent().size());
        assertTrue(page0.getTotalElements() >= 5050);

        Page<CodingProblem> dpProblems = codingProblemService.searchProblems(
                null, null, "Dynamic Programming", PageRequest.of(0, 20)
        );
        assertFalse(dpProblems.isEmpty());
        assertTrue(dpProblems.getContent().stream().allMatch(p ->
                p.getTags() != null && p.getTags().stream().anyMatch(t -> t.equalsIgnoreCase("Dynamic Programming"))
        ));

        Page<CodingProblem> easyBinarySearch = codingProblemService.searchProblems(
                null, "EASY", "Binary Search", PageRequest.of(0, 20)
        );
        assertFalse(easyBinarySearch.isEmpty());
        assertTrue(easyBinarySearch.getContent().stream().allMatch(p -> "EASY".equalsIgnoreCase(p.getDifficulty())));
    }

    @Test
    @DisplayName("Verify available tags query returns rich topic coverage")
    void testAvailableTags() {
        List<String> tags = codingProblemService.getAvailableTags();
        assertNotNull(tags);
        System.out.printf("Total distinct tags in system: %d%n", tags.size());
        assertTrue(tags.size() >= 25, "Expected at least 25 DSA tags, got: " + tags.size());
        assertTrue(tags.contains("Array"));
        assertTrue(tags.contains("Binary Search"));
        assertTrue(tags.contains("Graph"));
    }

    @Test
    @DisplayName("Verify public vs hidden test cases isolation")
    void testPublicAndHiddenTestCases() {
        Optional<CodingProblem> problemOpt = codingProblemRepository.findBySourceId("dsa-0001");
        assertTrue(problemOpt.isPresent());
        CodingProblem problem = problemOpt.get();

        List<CodingTestCase> publicTestCases = codingTestCaseRepository
                .findByProblemAndHiddenFalseAndActiveTrueOrderByTestCaseNumberAsc(problem);
        assertFalse(publicTestCases.isEmpty());

        List<CodingTestCase> allTestCases = codingTestCaseRepository
                .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(problem);
        assertEquals(4, allTestCases.size());

        long hiddenCount = allTestCases.stream().filter(CodingTestCase::isHidden).count();
        assertEquals(2, hiddenCount);
    }

    @Test
    @DisplayName("Verify wrapper service executes generated and legacy problems")
    void testExecutionWrapper() {
        Optional<CodingProblem> problemOpt = codingProblemRepository.findBySourceId("dsa-0001");
        assertTrue(problemOpt.isPresent());
        CodingProblem problem = problemOpt.get();

        String javaExecutable = wrapperService.buildExecutableCode(
                problem,
                "java",
                "public static int solve(int a, int b) { return a + b; }",
                "1 2"
        );
        assertNotNull(javaExecutable);
        assertTrue(javaExecutable.contains("Main"));

        String pyExecutable = wrapperService.buildExecutableCode(
                problem,
                "python",
                "def solve(a, b):\n    return a + b",
                "1 2"
        );
        assertNotNull(pyExecutable);
        assertTrue(pyExecutable.contains("solve"));
    }
}
