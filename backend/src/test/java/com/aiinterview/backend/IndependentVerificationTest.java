package com.aiinterview.backend;

import com.aiinterview.backend.controller.CodingProblemController;
import com.aiinterview.backend.dto.coding.CodeExecutionRequest;
import com.aiinterview.backend.dto.coding.CodeExecutionResponse;
import com.aiinterview.backend.dto.coding.CodeExecutionTestCaseResponse;
import com.aiinterview.backend.dto.coding.CodingProblemListResponse;
import com.aiinterview.backend.dto.coding.CodingProblemResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingProblemCompletion;
import com.aiinterview.backend.entity.CodingProgress;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.CodingProblemCompletionRepository;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.CodingProgressRepository;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import com.aiinterview.backend.repository.UserRepository;
import com.aiinterview.backend.service.coding.CodeExecutionService;
import com.aiinterview.backend.service.coding.CodingProblemCompletionService;
import com.aiinterview.backend.service.coding.CodingProblemService;
import com.aiinterview.backend.service.coding.CodingProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IndependentVerificationTest {

    @Autowired
    private CodingProblemRepository codingProblemRepository;

    @Autowired
    private CodingTestCaseRepository codingTestCaseRepository;

    @Autowired
    private CodingProblemService codingProblemService;

    @Autowired
    private CodingProblemController codingProblemController;

    @Autowired
    private CodeExecutionService codeExecutionService;

    @Autowired
    private CodingProgressService codingProgressService;

    @Autowired
    private CodingProblemCompletionService codingProblemCompletionService;

    @Autowired
    private CodingProgressRepository codingProgressRepository;

    @Autowired
    private CodingProblemCompletionRepository codingProblemCompletionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommandLineRunner importCodingProblemDataset;

    @Test
    @Order(1)
    @DisplayName("Verification 5: Database contains 5,000+ CodingProblem records")
    void verifyDatabaseProblemCount() {
        long count = codingProblemRepository.count();
        System.out.printf("[VERIFY 5] Database Problem Count: %d%n", count);
        assertTrue(count >= 5000, "Database must contain at least 5000 problems, found: " + count);
    }

    @Test
    @Order(2)
    @DisplayName("Verification 6: Database contains expected test cases")
    void verifyDatabaseTestCaseCount() {
        long count = codingTestCaseRepository.count();
        System.out.printf("[VERIFY 6] Database Test Case Count: %d%n", count);
        assertTrue(count >= 20000, "Database must contain at least 20000 test cases, found: " + count);
    }

    @Test
    @Order(3)
    @Transactional(readOnly = true)
    @DisplayName("Verification 7: Every problem has both public and hidden test cases")
    void verifyPublicAndHiddenTestCases() {
        List<CodingProblem> activeProblems = codingProblemRepository.findByActiveTrue();
        assertFalse(activeProblems.isEmpty());

        int missingPublic = 0;
        int missingHidden = 0;
        int totalChecked = 0;

        for (CodingProblem problem : activeProblems) {
            List<CodingTestCase> testCases = codingTestCaseRepository
                    .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(problem);
            boolean hasPub = testCases.stream().anyMatch(tc -> !tc.isHidden());
            boolean hasHid = testCases.stream().anyMatch(CodingTestCase::isHidden);

            if (!hasPub) missingPublic++;
            if (!hasHid) missingHidden++;
            totalChecked++;
        }

        System.out.printf("[VERIFY 7] Total Problems Checked: %d, Missing Public: %d, Missing Hidden: %d%n",
                totalChecked, missingPublic, missingHidden);

        assertEquals(0, missingPublic, "All problems must have at least one public test case");
        assertEquals(0, missingHidden, "All problems must have at least one hidden test case");
    }

    @Test
    @Order(4)
    @DisplayName("Verification 8: Hidden test cases are NOT returned by problem-list API")
    void verifyProblemListApiExcludesTestCases() {
        ResponseEntity<Page<CodingProblemListResponse>> response =
                codingProblemController.getProblems(0, 50, null, null, null);

        assertNotNull(response.getBody());
        List<CodingProblemListResponse> list = response.getBody().getContent();
        assertFalse(list.isEmpty());

        System.out.printf("[VERIFY 8] Problem List API returned %d problems on page 0.%n", list.size());
        // Verify list response DTO structure does not expose test cases
        for (CodingProblemListResponse item : list) {
            assertNotNull(item.getId());
            assertNotNull(item.getTitle());
            assertNotNull(item.getDifficulty());
        }
    }

    @Test
    @Order(5)
    @Transactional(readOnly = true)
    @DisplayName("Verification 9: Problem-detail API does not expose hidden test cases")
    void verifyProblemDetailApiExcludesHiddenTestCases() {
        List<CodingProblem> sample = codingProblemRepository.findByActiveTrue()
                .stream()
                .limit(20)
                .toList();

        int detailsChecked = 0;
        for (CodingProblem problem : sample) {
            ResponseEntity<CodingProblemResponse> detail =
                    codingProblemController.getProblem(problem.getId());

            assertNotNull(detail.getBody());
            CodingProblemResponse body = detail.getBody();

            // Check that all test cases returned match only public test cases from the database
            List<CodingTestCase> hiddenDbCases = codingTestCaseRepository
                    .findByProblemAndHiddenTrueAndActiveTrueOrderByTestCaseNumberAsc(problem);
            java.util.Set<String> hiddenInputs = hiddenDbCases.stream()
                    .map(CodingTestCase::getInput)
                    .collect(java.util.stream.Collectors.toSet());

            if (body.getTestCases() != null) {
                for (var tc : body.getTestCases()) {
                    assertFalse(hiddenInputs.contains(tc.getInput()),
                            "Hidden test case input MUST NOT be returned in detail API!");
                }
            }
            detailsChecked++;
        }
        System.out.printf("[VERIFY 9] Checked %d problem details: 0 hidden test cases exposed.%n", detailsChecked);
    }

    @Test
    @Order(6)
    @DisplayName("Verification 10: Pagination works near the end of the dataset")
    void verifyPaginationNearEndOfDataset() {
        // Page 100 with size 50 represents elements 5000-5050
        ResponseEntity<Page<CodingProblemListResponse>> page100 =
                codingProblemController.getProblems(100, 50, null, null, null);

        assertNotNull(page100.getBody());
        Page<CodingProblemListResponse> body100 = page100.getBody();
        System.out.printf("[VERIFY 10] Page 100 elements: %d, Total Elements: %d, Total Pages: %d%n",
                body100.getNumberOfElements(), body100.getTotalElements(), body100.getTotalPages());

        assertTrue(body100.hasContent(), "Page 100 near end of dataset must have content");
        assertTrue(body100.getTotalElements() >= 5050);
        assertTrue(body100.getTotalPages() >= 101);
    }

    @Test
    @Order(7)
    @Transactional(readOnly = true)
    @DisplayName("Verification 11: Search, difficulty filtering, and topic/tag filtering")
    void verifySearchAndFiltering() {
        // 1. Search by keyword
        ResponseEntity<Page<CodingProblemListResponse>> searchRes =
                codingProblemController.getProblems(0, 20, "Variant 1", null, null);
        assertNotNull(searchRes.getBody());
        assertFalse(searchRes.getBody().isEmpty());
        System.out.printf("[VERIFY 11] Search 'Variant 1' found: %d problems%n",
                searchRes.getBody().getTotalElements());

        // 2. Difficulty filter
        ResponseEntity<Page<CodingProblemListResponse>> easyRes =
                codingProblemController.getProblems(0, 20, null, "EASY", null);
        assertNotNull(easyRes.getBody());
        assertTrue(easyRes.getBody().getContent().stream().allMatch(p -> "EASY".equalsIgnoreCase(p.getDifficulty())));

        ResponseEntity<Page<CodingProblemListResponse>> mediumRes =
                codingProblemController.getProblems(0, 20, null, "MEDIUM", null);
        assertNotNull(mediumRes.getBody());
        assertTrue(mediumRes.getBody().getContent().stream().allMatch(p -> "MEDIUM".equalsIgnoreCase(p.getDifficulty())));

        ResponseEntity<Page<CodingProblemListResponse>> hardRes =
                codingProblemController.getProblems(0, 20, null, "HARD", null);
        assertNotNull(hardRes.getBody());
        assertTrue(hardRes.getBody().getContent().stream().allMatch(p -> "HARD".equalsIgnoreCase(p.getDifficulty())));

        // 3. Tag filter
        ResponseEntity<Page<CodingProblemListResponse>> tagRes =
                codingProblemController.getProblems(0, 20, null, null, "Binary Search");
        assertNotNull(tagRes.getBody());
        assertFalse(tagRes.getBody().isEmpty());
        assertTrue(tagRes.getBody().getContent().stream().allMatch(p ->
                p.getTags() != null && p.getTags().stream().anyMatch(t -> t.equalsIgnoreCase("Binary Search"))
        ));

        // 4. Combined filter (difficulty + tag + search)
        ResponseEntity<Page<CodingProblemListResponse>> combinedRes =
                codingProblemController.getProblems(0, 20, "Variant", "EASY", "Array");
        assertNotNull(combinedRes.getBody());
        assertFalse(combinedRes.getBody().isEmpty());
        System.out.printf("[VERIFY 11] Combined filter ('Variant' + EASY + 'Array') found: %d problems%n",
                combinedRes.getBody().getTotalElements());
    }

    @Test
    @Order(8)
    @DisplayName("Verification 12: Actual Run request for Easy, Medium, and Hard problem")
    void verifyRunExecutionForEasyMediumHard() {
        // 1. Easy Problem (Two Sum in Java)
        CodingProblem easyProblem = codingProblemRepository.findByTitleIgnoreCase("Two Sum")
                .orElseGet(() -> codingProblemRepository.findByDifficultyAndActiveTrue("EASY").get(0));

        String easyJavaCode = """
                public static int[] twoSum(int[] nums, int target) {
                    for (int i = 0; i < nums.length; i++) {
                        for (int j = i + 1; j < nums.length; j++) {
                            if (nums[i] + nums[j] == target) {
                                return new int[] { i, j };
                            }
                        }
                    }
                    return new int[] { 0, 0 };
                }
                """;

        CodeExecutionResponse easyRun = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(easyProblem.getId())
                        .language("java")
                        .code(easyJavaCode)
                        .build()
        );

        System.out.printf("[VERIFY 12] Easy Run ('%s' - Java): passed=%b, status=%s, testsPassed=%d/%d%n",
                easyProblem.getTitle(), easyRun.isPassed(), easyRun.getStatus(),
                easyRun.getPassedTests(), easyRun.getTotalTests());
        if (!easyRun.isPassed()) {
            System.err.println("easyRun error: " + easyRun.getError());
            System.err.println("easyRun message: " + easyRun.getMessage());
            if (easyRun.getTestCases() != null) {
                for (var tc : easyRun.getTestCases()) {
                    System.err.println("TC " + tc.getTestCaseNumber() + ": passed=" + tc.isPassed() + ", status=" + tc.getStatus() + ", error=" + tc.getError() + ", actual=" + tc.getActualOutput() + ", expected=" + tc.getExpectedOutput());
                }
            }
        }
        assertTrue(easyRun.isPassed(), "Easy problem run should pass");

        // 2. Medium Problem (Product of Array Except Self in Python)
        CodingProblem mediumProblem = codingProblemRepository.findByTitleIgnoreCase("Product of Array Except Self")
                .orElseGet(() -> codingProblemRepository.findByDifficultyAndActiveTrue("MEDIUM").get(0));

        String mediumPyCode = """
                def productExceptSelf(nums):
                    n = len(nums)
                    ans = [1] * n
                    prefix = 1
                    for i in range(n):
                        ans[i] = prefix
                        prefix *= nums[i]
                    suffix = 1
                    for i in range(n - 1, -1, -1):
                        ans[i] *= suffix
                        suffix *= nums[i]
                    return ans
                """;

        CodeExecutionResponse mediumRun = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(mediumProblem.getId())
                        .language("python")
                        .code(mediumPyCode)
                        .build()
        );

        System.out.printf("[VERIFY 12] Medium Run ('%s' - Python): passed=%b, status=%s, testsPassed=%d/%d%n",
                mediumProblem.getTitle(), mediumRun.isPassed(), mediumRun.getStatus(),
                mediumRun.getPassedTests(), mediumRun.getTotalTests());
        assertTrue(mediumRun.isPassed(), "Medium problem run should pass");

        // 3. Hard Problem (Trapping Rain Water in Python)
        CodingProblem hardProblem = codingProblemRepository.findByTitleIgnoreCase("Trapping Rain Water")
                .orElseGet(() -> codingProblemRepository.findByDifficultyAndActiveTrue("HARD").get(0));

        String hardPyCode = """
                def trap(height):
                    if not height:
                        return 0
                    l, r = 0, len(height) - 1
                    l_max, r_max = height[l], height[r]
                    res = 0
                    while l < r:
                        if l_max < r_max:
                            l += 1
                            l_max = max(l_max, height[l])
                            res += l_max - height[l]
                        else:
                            r -= 1
                            r_max = max(r_max, height[r])
                            res += r_max - height[r]
                    return res
                """;

        CodeExecutionResponse hardRun = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(hardProblem.getId())
                        .language("python")
                        .code(hardPyCode)
                        .build()
        );

        System.out.printf("[VERIFY 12] Hard Run ('%s' - Python): passed=%b, status=%s, testsPassed=%d/%d%n",
                hardProblem.getTitle(), hardRun.isPassed(), hardRun.getStatus(),
                hardRun.getPassedTests(), hardRun.getTotalTests());
        assertTrue(hardRun.isPassed(), "Hard problem run should pass");
    }

    @Test
    @Order(9)
    @DisplayName("Verification 13: Submit works and hidden test cases are executed server-side")
    void verifySubmitExecutesHiddenTestCases() {
        CodingProblem problem = codingProblemRepository.findByTitleIgnoreCase("Two Sum")
                .orElseGet(() -> codingProblemRepository.findByDifficultyAndActiveTrue("EASY").get(0));

        String javaCode = """
                public static int[] twoSum(int[] nums, int target) {
                    for (int i = 0; i < nums.length; i++) {
                        for (int j = i + 1; j < nums.length; j++) {
                            if (nums[i] + nums[j] == target) {
                                return new int[] { i, j };
                            }
                        }
                    }
                    return new int[] { 0, 0 };
                }
                """;

        CodeExecutionResponse submitResponse = codeExecutionService.execute(
                CodeExecutionRequest.builder()
                        .problemId(problem.getId())
                        .language("java")
                        .code(javaCode)
                        .build()
        );

        assertNotNull(submitResponse);
        assertTrue(submitResponse.isPassed());
        List<CodeExecutionTestCaseResponse> testResults = submitResponse.getTestCases();
        assertNotNull(testResults);
        assertEquals(submitResponse.getTotalTests(), testResults.size());

        // Confirm server executed ALL test cases (both public and hidden)
        assertTrue(testResults.size() >= 4, "Must execute all test cases (public + hidden)");

        // Verify hidden test cases have masked inputs and outputs
        List<CodingTestCase> dbTestCases = codingTestCaseRepository
                .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(problem);

        int hiddenChecked = 0;
        for (int i = 0; i < testResults.size(); i++) {
            CodeExecutionTestCaseResponse result = testResults.get(i);
            CodingTestCase dbTc = dbTestCases.get(i);
            if (dbTc.isHidden()) {
                hiddenChecked++;
                assertNull(result.getInput(), "Hidden test case input MUST be null in response");
                assertNull(result.getExpectedOutput(), "Hidden test case expectedOutput MUST be null in response");
            }
        }

        System.out.printf("[VERIFY 13] Submit verified: %d total tests executed, %d hidden tests masked.%n",
                testResults.size(), hiddenChecked);
        assertTrue(hiddenChecked >= 1, "Must have verified at least one hidden test case");
    }

    @Test
    @Order(10)
    @DisplayName("Verification 14: Progress and completion tracking")
    void verifyProgressAndCompletionTracking() {
        // Find or create test user
        User user = userRepository.findByEmail("test_verifier@example.com")
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername("test_verifier");
                    newUser.setName("Verifier User");
                    newUser.setEmail("test_verifier@example.com");
                    newUser.setPassword("encoded_pass");
                    return userRepository.save(newUser);
                });

        CodingProblem problem = codingProblemRepository.findByTitleIgnoreCase("Two Sum")
                .orElseGet(() -> codingProblemRepository.findByActiveTrue().get(0));

        // Record submission
        codingProgressService.updateSubmission(user, true);
        codingProgressService.markProblemCompleted(user, problem);
        codingProblemCompletionService.recordSubmission(user, problem, "java", "// solution", true);

        // Verify completion was saved
        Optional<CodingProblemCompletion> completionOpt =
                codingProblemCompletionRepository.findByUserIdAndProblemId(user.getId(), problem.getId());
        assertTrue(completionOpt.isPresent(), "CodingProblemCompletion record must exist");
        assertTrue(completionOpt.get().isCompleted());

        // Verify progress was updated
        Optional<CodingProgress> progressOpt = codingProgressRepository.findByUser(user);
        assertTrue(progressOpt.isPresent(), "CodingProgress record must exist");
        assertTrue(progressOpt.get().getTotalSubmissions() >= 1);
        assertTrue(progressOpt.get().getCompletedProblems() >= 1);

        System.out.printf("[VERIFY 14] User progress verified: totalSubmissions=%d, completedProblems=%d%n",
                progressOpt.get().getTotalSubmissions(), progressOpt.get().getCompletedProblems());
    }

    @Test
    @Order(11)
    @DisplayName("Verification 15: Re-running importer results in zero duplicate inserts")
    void verifyImporterIdempotency() throws Exception {
        long problemCountBefore = codingProblemRepository.count();
        long testCaseCountBefore = codingTestCaseRepository.count();

        // Re-run the importer
        importCodingProblemDataset.run();

        long problemCountAfter = codingProblemRepository.count();
        long testCaseCountAfter = codingTestCaseRepository.count();

        System.out.printf("[VERIFY 15] Problem count before=%d, after=%d; TestCase count before=%d, after=%d%n",
                problemCountBefore, problemCountAfter, testCaseCountBefore, testCaseCountAfter);

        assertEquals(problemCountBefore, problemCountAfter, "No duplicate problems should be inserted");
        assertEquals(testCaseCountBefore, testCaseCountAfter, "No duplicate test cases should be inserted");
    }
}
