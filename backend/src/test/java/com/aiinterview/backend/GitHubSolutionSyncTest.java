package com.aiinterview.backend;

import com.aiinterview.backend.config.CentralLanguageRegistry;
import com.aiinterview.backend.dto.coding.CodeExecutionRequest;
import com.aiinterview.backend.dto.coding.CodeExecutionResponse;
import com.aiinterview.backend.dto.coding.CodeExecutionTestCaseResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.entity.GitHubConnection;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import com.aiinterview.backend.service.coding.CodeExecutionService;
import com.aiinterview.backend.service.coding.GitHubSolutionHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GitHubSolutionSyncTest {

    @Autowired
    private CodingProblemRepository codingProblemRepository;

    @Autowired
    private CodingTestCaseRepository codingTestCaseRepository;

    @Autowired
    private CodeExecutionService codeExecutionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Phase 1: Run executes ONLY public test cases")
    void testRunExecutesOnlyPublicTestCases() {
        CodingProblem problem = codingProblemRepository.findBySourceId("dsa-0001")
                .orElseGet(() -> codingProblemRepository.findAll().get(0));
        assertNotNull(problem, "Test problem must exist");

        List<CodingTestCase> publicCases = codingTestCaseRepository
                .findByProblemAndHiddenFalseAndActiveTrueOrderByTestCaseNumberAsc(problem);
        List<CodingTestCase> allCases = codingTestCaseRepository
                .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(problem);

        assertTrue(publicCases.size() < allCases.size(), "Problem must have both public and hidden test cases");

        CodeExecutionRequest request = CodeExecutionRequest.builder()
                .problemId(problem.getId())
                .language("python")
                .code("""
                        import sys
                        lines = sys.stdin.read().strip().split()
                        if lines:
                            print(3)
                        """)
                .build();

        // RUN mode (isSubmit = false)
        CodeExecutionResponse runResponse = codeExecutionService.execute(request, false);
        assertNotNull(runResponse, "Run response must not be null");
        assertEquals(publicCases.size(), runResponse.getTotalTests(),
                "Run must execute ONLY public test cases");
        assertEquals(publicCases.size(), runResponse.getTestCases().size(),
                "Run test cases list must match public test count");

        // Public test cases must have visible input and expected output
        for (CodeExecutionTestCaseResponse tc : runResponse.getTestCases()) {
            assertNotNull(tc.getInput(), "Public test case input must be visible");
            assertNotNull(tc.getExpectedOutput(), "Public test case expected output must be visible");
        }
    }

    @Test
    @DisplayName("Phase 1: Submit executes public + hidden test cases with strict privacy")
    void testSubmitExecutesAllWithHiddenPrivacy() {
        CodingProblem problem = codingProblemRepository.findBySourceId("dsa-0001")
                .orElseGet(() -> codingProblemRepository.findAll().get(0));
        assertNotNull(problem, "Test problem must exist");

        List<CodingTestCase> allCases = codingTestCaseRepository
                .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(problem);

        CodeExecutionRequest request = CodeExecutionRequest.builder()
                .problemId(problem.getId())
                .language("python")
                .code("""
                        import sys
                        lines = sys.stdin.read().strip().split()
                        if lines:
                            print(3)
                        """)
                .build();

        // SUBMIT mode (isSubmit = true)
        CodeExecutionResponse submitResponse = codeExecutionService.execute(request, true);
        assertNotNull(submitResponse, "Submit response must not be null");
        assertEquals(allCases.size(), submitResponse.getTotalTests(),
                "Submit must execute ALL active test cases (public + hidden)");
        assertEquals(allCases.size(), submitResponse.getTestCases().size(),
                "Submit test cases list must match total test count");

        // Verify hidden test cases NEVER leak input, expectedOutput, or actualOutput
        int hiddenFound = 0;
        for (int i = 0; i < allCases.size(); i++) {
            CodingTestCase entityCase = allCases.get(i);
            CodeExecutionTestCaseResponse respCase = submitResponse.getTestCases().get(i);

            if (entityCase.isHidden()) {
                hiddenFound++;
                assertNull(respCase.getInput(), "Hidden test case input must be null");
                assertNull(respCase.getExpectedOutput(), "Hidden test case expectedOutput must be null");
                assertNull(respCase.getActualOutput(), "Hidden test case actualOutput must be null");
            } else {
                assertNotNull(respCase.getInput(), "Public test case input must be present");
                assertNotNull(respCase.getExpectedOutput(), "Public test case expectedOutput must be present");
            }
        }
        assertTrue(hiddenFound > 0, "At least one hidden test case must have been verified");
    }

    @Test
    @DisplayName("Phase 2: Mandatory Multi-Solution Progression (Submissions A through F)")
    void testMandatoryMultiSolutionProgression() {
        CodingProblem problem = CodingProblem.builder()
                .slug("two-sum")
                .title("Two Sum")
                .build();

        // SUBMISSION A (Java): Initial first solution -> Raw code only, no Solution 1 header
        String javaSolution1 = """
                class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        return new int[]{0, 1};
                    }
                }
                """.trim();

        GitHubSolutionHelper.MergeResult subA =
                GitHubSolutionHelper.prepareMergedContent(javaSolution1, "", "java");
        assertFalse(subA.duplicate(), "Submission A must not be duplicate");
        assertEquals(1, subA.solutionNumber(), "Submission A must be Solution 1");
        assertEquals(javaSolution1 + "\n", subA.content(), "First solution must be raw code without banner");
        assertFalse(subA.content().contains("SOLUTION 1"), "First solution must NOT contain SOLUTION 1 banner");

        // SUBMISSION B (Identical Java): Same code with CRLF / trailing space variation -> Detected as duplicate
        String javaSolution1Duplicate = javaSolution1.replace("\n", "\r\n") + "   \n";
        GitHubSolutionHelper.MergeResult subB =
                GitHubSolutionHelper.prepareMergedContent(javaSolution1Duplicate, subA.content(), "java");
        assertTrue(subB.duplicate(), "Submission B must be recognized as duplicate");
        assertEquals(1, subB.solutionNumber(), "Duplicate must reference solution number 1");
        assertEquals(subA.content(), subB.content(), "File content must remain unchanged on duplicate");

        // SUBMISSION C (Different Java): Second distinct solution -> Converts file into Solution 1 and Solution 2 with //
        String javaSolution2 = """
                import java.util.HashMap;
                class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        var map = new HashMap<Integer, Integer>();
                        for (int i = 0; i < nums.length; i++) {
                            int comp = target - nums[i];
                            if (map.containsKey(comp)) return new int[]{map.get(comp), i};
                            map.put(nums[i], i);
                        }
                        return new int[0];
                    }
                }
                """.trim();

        GitHubSolutionHelper.MergeResult subC =
                GitHubSolutionHelper.prepareMergedContent(javaSolution2, subA.content(), "java");
        assertFalse(subC.duplicate(), "Submission C must not be duplicate");
        assertEquals(2, subC.solutionNumber(), "Submission C must be Solution 2");
        assertTrue(subC.content().contains("// ================ SOLUTION 1 ================"),
                "Content must contain Solution 1 header with // prefix");
        assertTrue(subC.content().contains("// ================ SOLUTION 2 ================"),
                "Content must contain Solution 2 header with // prefix");
        assertTrue(subC.content().contains("twoSum(int[] nums, int target)"),
                "Content must contain original Solution 1 code");
        assertTrue(subC.content().contains("HashMap<Integer, Integer>"),
                "Content must contain new Solution 2 code");

        // SUBMISSION D (Identical to C): Exact match of Solution 2 -> Detected as duplicate
        GitHubSolutionHelper.MergeResult subD =
                GitHubSolutionHelper.prepareMergedContent(javaSolution2 + "\n\n", subC.content(), "java");
        assertTrue(subD.duplicate(), "Submission D must be recognized as duplicate");
        assertEquals(2, subD.solutionNumber(), "Duplicate must point to Solution 2");
        assertEquals(subC.content(), subD.content(), "File content must remain unchanged on duplicate");

        // SUBMISSION E (Third distinct Java): Appends Solution 3
        String javaSolution3 = """
                class Solution {
                    public int[] twoSum(int[] nums, int target) {
                        // Two-pointer approach
                        return new int[]{0, 1};
                    }
                }
                """.trim();

        GitHubSolutionHelper.MergeResult subE =
                GitHubSolutionHelper.prepareMergedContent(javaSolution3, subC.content(), "java");
        assertFalse(subE.duplicate(), "Submission E must not be duplicate");
        assertEquals(3, subE.solutionNumber(), "Submission E must be Solution 3");
        assertTrue(subE.content().contains("// ================ SOLUTION 1 ================"));
        assertTrue(subE.content().contains("// ================ SOLUTION 2 ================"));
        assertTrue(subE.content().contains("// ================ SOLUTION 3 ================"));
        assertTrue(subE.content().contains("// Two-pointer approach"));

        // SUBMISSION F (Python): Separate file path, uses # comment prefix
        String pythonPath = GitHubSolutionHelper.getSolutionPath(problem, "python");
        String javaPath = GitHubSolutionHelper.getSolutionPath(problem, "java");
        assertEquals("coding-solutions/two-sum/Solution.py", pythonPath,
                "Python file must have .py extension in separate file");
        assertEquals("coding-solutions/two-sum/Solution.java", javaPath,
                "Java file must have .java extension");
        assertNotEquals(pythonPath, javaPath, "Python and Java solutions must reside in distinct files");

        String pythonSolution1 = "def two_sum(nums, target):\n    return [0, 1]";
        GitHubSolutionHelper.MergeResult pySub1 =
                GitHubSolutionHelper.prepareMergedContent(pythonSolution1, "", "python");
        assertEquals(1, pySub1.solutionNumber());
        assertEquals(pythonSolution1 + "\n", pySub1.content());

        String pythonSolution2 = "def two_sum(nums, target):\n    seen = {}\n    return [seen[target - n], i]";
        GitHubSolutionHelper.MergeResult pySub2 =
                GitHubSolutionHelper.prepareMergedContent(pythonSolution2, pySub1.content(), "python");
        assertEquals(2, pySub2.solutionNumber());
        assertTrue(pySub2.content().contains("# ================ SOLUTION 1 ================"),
                "Python multi-solution header must use # comment prefix");
        assertTrue(pySub2.content().contains("# ================ SOLUTION 2 ================"),
                "Python multi-solution header must use # comment prefix");
    }

    @Test
    @DisplayName("Phase 2: Comment Syntax Across Language Families")
    void testLanguageCommentSyntaxAcrossFamilies() {
        // Hash (#) comment languages
        assertEquals("#", GitHubSolutionHelper.getCommentPrefix("python"));
        assertEquals("#", GitHubSolutionHelper.getCommentPrefix("ruby"));
        assertEquals("#", GitHubSolutionHelper.getCommentPrefix("bash"));
        assertEquals("#", GitHubSolutionHelper.getCommentPrefix("elixir"));
        assertEquals("#", GitHubSolutionHelper.getCommentPrefix("perl"));
        assertEquals("#", GitHubSolutionHelper.getCommentPrefix("r"));
        assertEquals("#", GitHubSolutionHelper.getCommentPrefix("julia"));
        assertEquals("#", GitHubSolutionHelper.getCommentPrefix("nim"));

        // Dash-dash (--) comment languages
        assertEquals("--", GitHubSolutionHelper.getCommentPrefix("lua"));
        assertEquals("--", GitHubSolutionHelper.getCommentPrefix("haskell"));

        // Semicolon (;) comment languages
        assertEquals(";", GitHubSolutionHelper.getCommentPrefix("racket"));

        // Percent (%) comment languages
        assertEquals("%", GitHubSolutionHelper.getCommentPrefix("erlang"));

        // COBOL (*>) comment languages
        assertEquals("*>", GitHubSolutionHelper.getCommentPrefix("cobol"));

        // OCaml ((* ... *)) comment languages
        assertEquals("(*", GitHubSolutionHelper.getCommentPrefix("ocaml"));
        String ocamlHeader = GitHubSolutionHelper.formatSolutionHeader(1, "ocaml");
        assertEquals("(* ================ SOLUTION 1 ================ *)", ocamlHeader);

        // Slash-slash (//) comment languages
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("java"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("cpp"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("c"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("csharp"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("javascript"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("typescript"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("go"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("rust"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("kotlin"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("swift"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("php"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("scala"));
        assertEquals("//", GitHubSolutionHelper.getCommentPrefix("dart"));
    }

    @Test
    @DisplayName("Phase 2: Path and Slug Sanitization Prevents Traversal")
    void testSlugAndPathSanitization() {
        assertEquals("two-sum", GitHubSolutionHelper.sanitizeProblemSlug("two-sum", "Two Sum"));
        assertEquals("problem-with-symbols", GitHubSolutionHelper.sanitizeProblemSlug("", "Problem with @#$% Symbols!"));
        assertEquals("etc-safe-slug", GitHubSolutionHelper.sanitizeProblemSlug("../../../etc/safe-slug", "Title"));
        assertFalse(GitHubSolutionHelper.sanitizeProblemSlug("../../passwd", "Test").contains(".."));

        CodingProblem problem = CodingProblem.builder()
                .slug("../sneaky-hack/../two-sum")
                .title("Two Sum")
                .build();
        String path = GitHubSolutionHelper.getSolutionPath(problem, "java");
        assertFalse(path.contains(".."), "Solution path must not contain path traversal dots");
        assertTrue(path.startsWith("coding-solutions/"), "Path must start with coding-solutions/");
        assertTrue(path.endsWith("/Solution.java"), "Path must end with Solution.java");
    }

    @Test
    @DisplayName("Phase 2: Security — AccessToken is @JsonIgnore and not serialized")
    void testAccessTokenNotSerialized() throws Exception {
        GitHubConnection connection = new GitHubConnection();
        connection.setGithubUsername("octocat");
        connection.setAccessToken("ghp_secret_token_1234567890abcdef");
        connection.setRepositoryUrl("https://github.com/octocat/solutions");

        String json = objectMapper.writeValueAsString(connection);
        assertFalse(json.contains("ghp_secret_token_1234567890abcdef"),
                "Access token must NEVER be serialized into JSON");
        assertFalse(json.contains("accessToken"),
                "accessToken field must be ignored during JSON serialization");
        assertTrue(json.contains("https://github.com/octocat/solutions"),
                "Non-sensitive fields like repositoryUrl should be present");
    }

    @Test
    @DisplayName("Phase 2: All 51 Languages Have Valid Solution File Extensions")
    void testAll32LanguagesHaveFileExtensions() {
        List<CentralLanguageRegistry.LanguageSpec> all = CentralLanguageRegistry.getAllLanguages();
        assertEquals(51, all.size(), "CentralLanguageRegistry must contain all 51 languages");

        for (CentralLanguageRegistry.LanguageSpec spec : all) {
            String fileName = GitHubSolutionHelper.getSolutionFileName(spec.key());
            assertTrue(fileName.startsWith("Solution."),
                    "File name for " + spec.key() + " must start with 'Solution.'");
            assertTrue(fileName.length() > "Solution.".length(),
                    "File extension for " + spec.key() + " must not be empty");
            assertNotEquals(".txt", fileName.substring("Solution".length()),
                    "Known language " + spec.key() + " should have a recognized extension");
        }
    }
}
