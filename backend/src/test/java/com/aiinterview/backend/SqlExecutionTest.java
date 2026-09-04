package com.aiinterview.backend;

import com.aiinterview.backend.dto.coding.CodeExecutionRequest;
import com.aiinterview.backend.dto.coding.CodeExecutionResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.service.coding.CodeExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SqlExecutionTest {

    @Autowired
    private CodingProblemRepository codingProblemRepository;

    @Autowired
    private CodeExecutionService codeExecutionService;

    @Test
    void testSqlRunPublicTestsOnly() {
        CodingProblem sqlProblem = codingProblemRepository.findBySourceId("sql-0001")
                .orElseThrow(() -> new IllegalStateException("sql-0001 must exist in DB"));

        assertEquals("DATABASE", sqlProblem.getCategory());

        // Valid reference query: SELECT * FROM Employees WHERE id <= 3 ORDER BY id ASC;
        String query = "SELECT * FROM Employees WHERE id <= 3 ORDER BY id ASC;";

        CodeExecutionResponse response = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(sqlProblem.getId())
                        .language("mysql")
                        .code(query)
                        .build(),
                false // isSubmit = false (RUN)
        );

        assertNotNull(response);
        assertEquals("ACCEPTED", response.getStatus(), "Status should be ACCEPTED: " + response.getError());
        assertTrue(response.isPassed());
        assertEquals(1, response.getTotalTests(), "Run mode should only execute 1 public test case");
        assertEquals(1, response.getPassedTests());
        assertNotNull(response.getTestCases());
        assertEquals(1, response.getTestCases().size());
        assertNotNull(response.getTestCases().get(0).getActualOutput());
    }

    @Test
    void testSqlSubmitRunsPublicAndHiddenWithPrivacy() {
        CodingProblem sqlProblem = codingProblemRepository.findBySourceId("sql-0001")
                .orElseThrow(() -> new IllegalStateException("sql-0001 must exist in DB"));

        String query = "SELECT * FROM Employees WHERE id <= 3 ORDER BY id ASC;";

        CodeExecutionResponse response = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(sqlProblem.getId())
                        .language("mysql")
                        .code(query)
                        .build(),
                true // isSubmit = true (SUBMIT)
        );

        assertNotNull(response);
        assertEquals("ACCEPTED", response.getStatus());
        assertTrue(response.isPassed());
        assertEquals(2, response.getTotalTests(), "Submit mode should execute all 2 test cases");
        assertEquals(2, response.getPassedTests());

        // Check hidden test case privacy
        var testCases = response.getTestCases();
        assertEquals(2, testCases.size());

        // Test case 1 is public: has input, expectedOutput, actualOutput
        assertNotNull(testCases.get(0).getInput());
        assertNotNull(testCases.get(0).getExpectedOutput());
        assertNotNull(testCases.get(0).getActualOutput());

        // Test case 2 is hidden: input, expectedOutput, actualOutput MUST be masked (null)
        assertNull(testCases.get(1).getInput(), "Hidden test case input must be masked");
        assertNull(testCases.get(1).getExpectedOutput(), "Hidden test case expectedOutput must be masked");
        assertNull(testCases.get(1).getActualOutput(), "Hidden test case actualOutput must be masked");
    }

    @Test
    void testSqlWrongAnswer() {
        CodingProblem sqlProblem = codingProblemRepository.findBySourceId("sql-0001")
                .orElseThrow(() -> new IllegalStateException("sql-0001 must exist in DB"));

        // Query returning wrong data
        String wrongQuery = "SELECT * FROM Employees WHERE id > 100 ORDER BY id ASC;";

        CodeExecutionResponse response = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(sqlProblem.getId())
                        .language("mysql")
                        .code(wrongQuery)
                        .build(),
                false
        );

        assertNotNull(response);
        assertEquals("WRONG_ANSWER", response.getStatus());
        assertFalse(response.isPassed());
        assertEquals(0, response.getPassedTests());
    }

    @Test
    void testSqlSecurityViolationBlocksDestructiveQuery() {
        CodingProblem sqlProblem = codingProblemRepository.findBySourceId("sql-0001")
                .orElseThrow(() -> new IllegalStateException("sql-0001 must exist in DB"));

        String maliciousQuery = "DROP DATABASE ai_platform_sandbox;";

        CodeExecutionResponse response = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(sqlProblem.getId())
                        .language("mysql")
                        .code(maliciousQuery)
                        .build(),
                false
        );

        assertNotNull(response);
        assertEquals("SECURITY_VIOLATION", response.getStatus());
        assertFalse(response.isPassed());
        assertTrue(response.getError().toLowerCase().contains("forbidden")
                || response.getError().toLowerCase().contains("security"));
    }

    @Test
    void testSqlSecurityBlocksAccessToProductionDatabase() {
        CodingProblem sqlProblem = codingProblemRepository.findBySourceId("sql-0001")
                .orElseThrow(() -> new IllegalStateException("sql-0001 must exist in DB"));

        String maliciousQuery = "SELECT * FROM ai_platform.users;";

        CodeExecutionResponse response = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(sqlProblem.getId())
                        .language("mysql")
                        .code(maliciousQuery)
                        .build(),
                false
        );

        assertNotNull(response);
        assertEquals("SECURITY_VIOLATION", response.getStatus());
        assertFalse(response.isPassed());
        assertTrue(response.getError().contains("ai_platform"));
    }
}
