package com.aiinterview.backend.config;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.annotation.Order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
public class CodingProblemSeeder {

    private static final List<String> MANAGED_TITLES = List.of(
            "Two Sum",
            "Valid Parentheses",
            "Best Time to Buy and Sell Stock",
            "Binary Search",
            "Longest Substring Without Repeating Characters",
            "Product of Array Except Self",
            "Merge Intervals",
            "Number of Islands",
            "Course Schedule",
            "Trapping Rain Water"
    );

    private final ObjectMapper objectMapper;

    public CodingProblemSeeder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    @Order(2)
    CommandLineRunner seedCodingProblems(
            CodingProblemRepository repository
    ) {

        return args -> {

            if (repository.count() == 0) {

                repository.saveAll(
                        List.of(
                                twoSum(),
                                validParentheses(),
                                bestTimeToBuyAndSellStock(),
                                binarySearch(),
                                longestSubstring(),
                                productExceptSelf(),
                                mergeIntervals(),
                                numberOfIslands(),
                                courseSchedule(),
                                trappingRainWater()
                        )
                );

                return;
            }

            for (String managedTitle : MANAGED_TITLES) {
                Optional<CodingProblem> existing = repository.findByTitleIgnoreCase(managedTitle);
                if (existing.isPresent()) {
                    CodingProblem problem = existing.get();
                    boolean changed = false;
                    applyFunctionMetadata(problem);

                    if (problem.getLanguageConfigurations() == null || problem.getLanguageConfigurations().isBlank()) {
                        applyProblemConfiguration(problem);
                        changed = true;
                    } else {
                        Map<String, Object> configurations = readConfigurations(problem.getLanguageConfigurations());
                        Map<String, Object> normalized = normalizeConfigurations(configurations);
                        String json = writeJson(normalized);
                        if (!json.equals(problem.getLanguageConfigurations())) {
                            problem.setLanguageConfigurations(json);
                            changed = true;
                        }
                    }

                    if (changed) {
                        repository.save(problem);
                    }
                }
            }
        };
    }

    private boolean isManagedProblem(String title) {

        return title.contains("two sum") ||
                title.contains("valid parentheses") ||
                title.contains("best time") ||
                title.contains("binary search") ||
                title.contains("longest substring") ||
                title.contains("product of array") ||
                title.contains("merge intervals") ||
                title.contains("number of islands") ||
                title.contains("course schedule") ||
                title.contains("trapping rain");
    }

    private Map<String, Object> readConfigurations(
            String json
    ) {

        try {

            Map<String, Object> configurations =
                    objectMapper.readValue(
                            json,
                            new TypeReference<Map<String, Object>>() {
                            }
                    );

            return configurations == null
                    ? new LinkedHashMap<>()
                    : configurations;

        } catch (Exception exception) {

            return new LinkedHashMap<>();
        }
    }

    private Map<String, Object> normalizeConfigurations(
            Map<String, Object> configurations
    ) {

        if (configurations == null) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> normalized =
                new LinkedHashMap<>();

        for (
                Map.Entry<String, Object> entry :
                configurations.entrySet()
        ) {

            if (
                    entry.getKey() == null ||
                    entry.getKey().isBlank() ||
                    entry.getValue() == null
            ) {
                continue;
            }

            String language =
                    entry.getKey()
                            .trim()
                            .toLowerCase();

            if (!(entry.getValue() instanceof Map<?, ?> rawMap)) {
                continue;
            }

            Map<String, Object> config =
                    new LinkedHashMap<>();

            for (
                    Map.Entry<?, ?> configEntry :
                    rawMap.entrySet()
            ) {

                if (
                        configEntry.getKey() == null ||
                        configEntry.getValue() == null
                ) {
                    continue;
                }

                config.put(
                        String.valueOf(
                                configEntry.getKey()
                        ),
                        configEntry.getValue()
                );
            }

            String displayName =
                    getString(
                            config,
                            "displayName",
                            capitalize(language)
                    );

            String runtimeLanguage =
                    getString(
                            config,
                            "runtimeLanguage",
                            language
                    );

            String fileName =
                    getString(
                            config,
                            "fileName",
                            defaultFileName(language)
                    );

            String starterCode =
                    getString(
                            config,
                            "starterCode",
                            ""
                    );

            String executionTemplate =
                    getString(
                            config,
                            "executionTemplate",
                            "{{USER_CODE}}"
                    );

            String monacoLanguage =
                    getString(
                            config,
                            "monacoLanguage",
                            language
                    );

            Map<String, Object> normalizedConfig =
                    language(
                            displayName,
                            runtimeLanguage,
                            fileName,
                            starterCode,
                            executionTemplate,
                            monacoLanguage
                    );

            Object runtimeVersion =
                    config.get("runtimeVersion");

            if (
                    runtimeVersion != null &&
                    !String.valueOf(
                            runtimeVersion
                    ).isBlank()
            ) {

                normalizedConfig.put(
                        "runtimeVersion",
                        String.valueOf(
                                runtimeVersion
                        ).trim()
                );
            }

            normalized.put(
                    language,
                    normalizedConfig
            );
        }

        return normalized;
    }

    private String getString(
            Map<String, Object> map,
            String key,
            String defaultValue
    ) {

        Object value = map.get(key);

        if (value == null) {
            return defaultValue;
        }

        String result =
                String.valueOf(value).trim();

        return result.isBlank()
                ? defaultValue
                : result;
    }

    private CodingProblem twoSum() {

        return base(
                "Two Sum",
                "Given an array of integers and a target value, return the indices of the two numbers that add up to the target.",
                "EASY",
                List.of("Array", "Hash Table"),
                "nums = [2,7,11,15], target = 9",
                "[0,1]",
                List.of(
                        "2 <= nums.length <= 10^4",
                        "-10^9 <= nums[i] <= 10^9",
                        "-10^9 <= target <= 10^9"
                ),
                "twoSum",
                "twoSum(nums, target)",
                "array",
                "array,integer",
                1
        );
    }

    private CodingProblem validParentheses() {

        return base(
                "Valid Parentheses",
                "Given a string containing brackets, determine whether the input string is valid. Every opening bracket must be closed by the same type of bracket in the correct order.",
                "EASY",
                List.of("String", "Stack"),
                "s = \"()[]{}\"",
                "true",
                List.of(
                        "1 <= s.length <= 10^4",
                        "s consists only of parentheses characters."
                ),
                "isValid",
                "isValid(s)",
                "boolean",
                "string",
                1
        );
    }

    private CodingProblem bestTimeToBuyAndSellStock() {

        return base(
                "Best Time to Buy and Sell Stock",
                "Given an array where prices[i] is the price of a stock on day i, find the maximum profit that can be achieved by buying on one day and selling on a later day.",
                "EASY",
                List.of("Array", "Greedy"),
                "prices = [7,1,5,3,6,4]",
                "5",
                List.of(
                        "1 <= prices.length <= 10^5",
                        "0 <= prices[i] <= 10^4"
                ),
                "maxProfit",
                "maxProfit(prices)",
                "integer",
                "integer[]",
                1
        );
    }

    private CodingProblem binarySearch() {

        return base(
                "Binary Search",
                "Given a sorted array of integers and a target value, return the index of the target if it exists. Otherwise return -1.",
                "MEDIUM",
                List.of("Array", "Binary Search"),
                "nums = [-1,0,3,5,9,12], target = 9",
                "4",
                List.of(
                        "1 <= nums.length <= 10^4",
                        "-10^4 <= nums[i], target <= 10^4",
                        "All integers in nums are unique.",
                        "nums is sorted in ascending order."
                ),
                "search",
                "search(nums, target)",
                "integer",
                "integer[],integer",
                2
        );
    }

    private CodingProblem longestSubstring() {

        return base(
                "Longest Substring Without Repeating Characters",
                "Given a string, find the length of the longest substring without repeating characters.",
                "MEDIUM",
                List.of("String", "Sliding Window", "Hash Table"),
                "s = \"abcabcbb\"",
                "3",
                List.of(
                        "0 <= s.length <= 5 * 10^4",
                        "s consists of English letters, digits, symbols and spaces."
                ),
                "lengthOfLongestSubstring",
                "lengthOfLongestSubstring(s)",
                "integer",
                "string",
                2
        );
    }

    private CodingProblem productExceptSelf() {

        return base(
                "Product of Array Except Self",
                "Given an integer array, return an array where each element is the product of all elements except the element at the same index.",
                "MEDIUM",
                List.of("Array", "Prefix Sum"),
                "nums = [1,2,3,4]",
                "[24,12,8,6]",
                List.of(
                        "2 <= nums.length <= 10^5",
                        "-30 <= nums[i] <= 30",
                        "The product of any prefix or suffix fits in a 32-bit integer."
                ),
                "productExceptSelf",
                "productExceptSelf(nums)",
                "integer[]",
                "integer[]",
                2
        );
    }

    private CodingProblem mergeIntervals() {

        return base(
                "Merge Intervals",
                "Given an array of intervals, merge all overlapping intervals and return an array of the non-overlapping intervals that cover all the input intervals.",
                "MEDIUM",
                List.of("Array", "Sorting", "Intervals"),
                "intervals = [[1,3],[2,6],[8,10],[15,18]]",
                "[[1,6],[8,10],[15,18]]",
                List.of(
                        "1 <= intervals.length <= 10^4",
                        "intervals[i].length == 2",
                        "0 <= start <= end <= 10^4"
                ),
                "merge",
                "merge(intervals)",
                "integer[][]",
                "integer[][]",
                2
        );
    }

    private CodingProblem numberOfIslands() {

        return base(
                "Number of Islands",
                "Given a grid of land and water cells, count the number of islands. An island is formed by connecting adjacent land cells horizontally or vertically.",
                "HARD",
                List.of("Graph", "DFS", "BFS", "Matrix"),
                "grid = [[\"1\",\"1\",\"0\"],[\"1\",\"0\",\"0\"],[\"0\",\"0\",\"1\"]]",
                "2",
                List.of(
                        "1 <= rows, columns <= 300",
                        "grid[i][j] is either '0' or '1'."
                ),
                "numIslands",
                "numIslands(grid)",
                "integer",
                "character[][]",
                3
        );
    }

    private CodingProblem courseSchedule() {

        return base(
                "Course Schedule",
                "There are a number of courses to take. Some courses have prerequisites. Determine whether it is possible to finish all courses.",
                "HARD",
                List.of("Graph", "BFS", "DFS", "Topological Sort"),
                "numCourses = 2, prerequisites = [[1,0]]",
                "true",
                List.of(
                        "1 <= numCourses <= 2000",
                        "0 <= prerequisites.length <= 5000",
                        "prerequisites[i].length == 2"
                ),
                "canFinish",
                "canFinish(numCourses, prerequisites)",
                "boolean",
                "integer,integer[][]",
                3
        );
    }

    private CodingProblem trappingRainWater() {

        return base(
                "Trapping Rain Water",
                "Given an array representing elevation heights, calculate how much rain water can be trapped after raining.",
                "HARD",
                List.of("Array", "Two Pointers", "Stack"),
                "height = [0,1,0,2,1,0,1,3,2,1,2,1]",
                "6",
                List.of(
                        "1 <= height.length <= 2 * 10^4",
                        "0 <= height[i] <= 10^5"
                ),
                "trap",
                "trap(height)",
                "integer",
                "integer[]",
                3
        );
    }

    private CodingProblem base(
            String title,
            String description,
            String difficulty,
            List<String> tags,
            String inputExample,
            String outputExample,
            List<String> constraints,
            String functionName,
            String functionSignature,
            String returnType,
            String parameterTypes,
            Integer experience
    ) {

        CodingProblem problem =
                CodingProblem.builder()
                        .title(title)
                        .description(description)
                        .difficulty(difficulty)
                        .tags(tags)
                        .inputExample(inputExample)
                        .outputExample(outputExample)
                        .constraints(constraints)
                        .functionName(functionName)
                        .functionSignature(functionSignature)
                        .returnType(returnType)
                        .parameterTypes(parameterTypes)
                        .starterCode("")
                        .minimumExperienceLevel(experience)
                        .active(true)
                        .build();

        applyProblemConfiguration(problem);

        return problem;
    }

    private void applyFunctionMetadata(
            CodingProblem problem
    ) {

        String title =
                problem.getTitle() == null
                        ? ""
                        : problem.getTitle()
                                .trim()
                                .toLowerCase();

        if (title.contains("two sum")) {

            problem.setFunctionName("twoSum");
            problem.setFunctionSignature(
                    "twoSum(nums, target)"
            );
            problem.setReturnType("array");
            problem.setParameterTypes(
                    "array,integer"
            );

            return;
        }

        if (title.contains("valid parentheses")) {

            problem.setFunctionName("isValid");
            problem.setFunctionSignature(
                    "isValid(s)"
            );
            problem.setReturnType("boolean");
            problem.setParameterTypes(
                    "string"
            );

            return;
        }

        if (title.contains("best time")) {

            problem.setFunctionName("maxProfit");
            problem.setFunctionSignature(
                    "maxProfit(prices)"
            );
            problem.setReturnType("integer");
            problem.setParameterTypes(
                    "integer[]"
            );

            return;
        }

        if (title.contains("binary search")) {

            problem.setFunctionName("search");
            problem.setFunctionSignature(
                    "search(nums, target)"
            );
            problem.setReturnType("integer");
            problem.setParameterTypes(
                    "integer[],integer"
            );

            return;
        }

        if (title.contains("longest substring")) {

            problem.setFunctionName(
                    "lengthOfLongestSubstring"
            );
            problem.setFunctionSignature(
                    "lengthOfLongestSubstring(s)"
            );
            problem.setReturnType("integer");
            problem.setParameterTypes("string");

            return;
        }

        if (title.contains("product of array")) {

            problem.setFunctionName(
                    "productExceptSelf"
            );
            problem.setFunctionSignature(
                    "productExceptSelf(nums)"
            );
            problem.setReturnType("integer[]");
            problem.setParameterTypes("integer[]");

            return;
        }

        if (title.contains("merge intervals")) {

            problem.setFunctionName("merge");
            problem.setFunctionSignature(
                    "merge(intervals)"
            );
            problem.setReturnType("integer[][]");
            problem.setParameterTypes("integer[][]");

            return;
        }

        if (title.contains("number of islands")) {

            problem.setFunctionName("numIslands");
            problem.setFunctionSignature(
                    "numIslands(grid)"
            );
            problem.setReturnType("integer");
            problem.setParameterTypes("character[][]");

            return;
        }

        if (title.contains("course schedule")) {

            problem.setFunctionName("canFinish");
            problem.setFunctionSignature(
                    "canFinish(numCourses, prerequisites)"
            );
            problem.setReturnType("boolean");
            problem.setParameterTypes(
                    "integer,integer[][]"
            );

            return;
        }

        if (title.contains("trapping rain")) {

            problem.setFunctionName("trap");
            problem.setFunctionSignature(
                    "trap(height)"
            );
            problem.setReturnType("integer");
            problem.setParameterTypes("integer[]");
        }
    }

    private void applyProblemConfiguration(
            CodingProblem problem
    ) {

        Map<String, Object> configurations =
                new LinkedHashMap<>();

        configurations.put(
                "java",
                language(
                        "Java",
                        "java",
                        "Main.java",
                        javaStarterCode(problem),
                        "{{USER_CODE}}",
                        "java"
                )
        );

        configurations.put(
                "python",
                language(
                        "Python",
                        "python",
                        "main.py",
                        pythonStarterCode(problem),
                        "{{USER_CODE}}",
                        "python"
                )
        );

        configurations.put(
                "kotlin",
                language(
                        "Kotlin",
                        "kotlin",
                        "Main.kt",
                        kotlinStarterCode(problem),
                        "{{USER_CODE}}",
                        "kotlin"
                )
        );

        configurations.put(
                "go",
                language(
                        "Go",
                        "go",
                        "main.go",
                        goStarterCode(problem),
                        "{{USER_CODE}}",
                        "go"
                )
        );

        configurations.put(
                "rust",
                language(
                        "Rust",
                        "rust",
                        "main.rs",
                        rustStarterCode(problem),
                        "{{USER_CODE}}",
                        "rust"
                )
        );

        problem.setLanguageConfigurations(
                writeJson(configurations)
        );

        problem.setStarterCode("");
    }

    private Map<String, Object> language(
            String displayName,
            String runtimeLanguage,
            String fileName,
            String starterCode,
            String executionTemplate,
            String monacoLanguage
    ) {

        Map<String, Object> configuration =
                new LinkedHashMap<>();

        configuration.put(
                "displayName",
                displayName
        );

        configuration.put(
                "runtimeLanguage",
                runtimeLanguage
        );

        configuration.put(
                "fileName",
                fileName
        );

        configuration.put(
                "monacoLanguage",
                monacoLanguage
        );

        configuration.put(
                "starterCode",
                starterCode
        );

        configuration.put(
                "executionTemplate",
                executionTemplate
        );

        return configuration;
    }

    private String defaultFileName(
            String language
    ) {

        return switch (language) {

            case "java" -> "Main.java";
            case "python" -> "main.py";
            case "kotlin" -> "Main.kt";
            case "go" -> "main.go";
            case "rust" -> "main.rs";
            default -> "solution";
        };
    }

    private String capitalize(
            String value
    ) {

        if (
                value == null ||
                value.isBlank()
        ) {
            return "";
        }

        return value.substring(0, 1).toUpperCase()
                + value.substring(1);
    }

    private String javaStarterCode(
            CodingProblem problem
    ) {

        return switch (problem.getFunctionName()) {

            case "twoSum" -> """
public static int[] twoSum(int[] nums, int target) {
    return new int[]{};
}
""";

            case "isValid" -> """
public static boolean isValid(String s) {
    return false;
}
""";

            case "maxProfit" -> """
public static int maxProfit(int[] prices) {
    return 0;
}
""";

            case "search" -> """
public static int search(int[] nums, int target) {
    return -1;
}
""";

            case "lengthOfLongestSubstring" -> """
public static int lengthOfLongestSubstring(String s) {
    return 0;
}
""";

            case "productExceptSelf" -> """
public static int[] productExceptSelf(int[] nums) {
    return new int[]{};
}
""";

            case "merge" -> """
public static int[][] merge(int[][] intervals) {
    return new int[][]{};
}
""";

            case "numIslands" -> """
public static int numIslands(char[][] grid) {
    return 0;
}
""";

            case "canFinish" -> """
public static boolean canFinish(int numCourses, int[][] prerequisites) {
    return false;
}
""";

            case "trap" -> """
public static int trap(int[] height) {
    return 0;
}
""";

            default -> """
public static void solve() {
}
""";
        };
    }

    private String pythonStarterCode(
            CodingProblem problem
    ) {

        return switch (problem.getFunctionName()) {

            case "twoSum" -> """
def twoSum(nums, target):
    return []
""";

            case "isValid" -> """
def isValid(s):
    return False
""";

            case "maxProfit" -> """
def maxProfit(prices):
    return 0
""";

            case "search" -> """
def search(nums, target):
    return -1
""";

            case "lengthOfLongestSubstring" -> """
def lengthOfLongestSubstring(s):
    return 0
""";

            case "productExceptSelf" -> """
def productExceptSelf(nums):
    return []
""";

            case "merge" -> """
def merge(intervals):
    return []
""";

            case "numIslands" -> """
def numIslands(grid):
    return 0
""";

            case "canFinish" -> """
def canFinish(numCourses, prerequisites):
    return False
""";

            case "trap" -> """
def trap(height):
    return 0
""";

            default -> """
def solve():
    pass
""";
        };
    }

    private String kotlinStarterCode(
            CodingProblem problem
    ) {

        return switch (problem.getFunctionName()) {

            case "twoSum" -> """
fun twoSum(nums: IntArray, target: Int): IntArray {
    return intArrayOf()
}
""";

            case "isValid" -> """
fun isValid(s: String): Boolean {
    return false
}
""";

            case "maxProfit" -> """
fun maxProfit(prices: IntArray): Int {
    return 0
}
""";

            case "search" -> """
fun search(nums: IntArray, target: Int): Int {
    return -1
}
""";

            case "lengthOfLongestSubstring" -> """
fun lengthOfLongestSubstring(s: String): Int {
    return 0
}
""";

            case "productExceptSelf" -> """
fun productExceptSelf(nums: IntArray): IntArray {
    return intArrayOf()
}
""";

            case "merge" -> """
fun merge(intervals: Array<IntArray>): Array<IntArray> {
    return emptyArray()
}
""";

            case "numIslands" -> """
fun numIslands(grid: Array<CharArray>): Int {
    return 0
}
""";

            case "canFinish" -> """
fun canFinish(numCourses: Int, prerequisites: Array<IntArray>): Boolean {
    return false
}
""";

            case "trap" -> """
fun trap(height: IntArray): Int {
    return 0
}
""";

            default -> """
fun solve() {
}
""";
        };
    }

    private String goStarterCode(
            CodingProblem problem
    ) {

        return switch (problem.getFunctionName()) {

            case "twoSum" -> """
func twoSum(nums []int, target int) []int {
    return []int{}
}
""";

            case "isValid" -> """
func isValid(s string) bool {
    return false
}
""";

            case "maxProfit" -> """
func maxProfit(prices []int) int {
    return 0
}
""";

            case "search" -> """
func search(nums []int, target int) int {
    return -1
}
""";

            case "lengthOfLongestSubstring" -> """
func lengthOfLongestSubstring(s string) int {
    return 0
}
""";

            case "productExceptSelf" -> """
func productExceptSelf(nums []int) []int {
    return []int{}
}
""";

            case "merge" -> """
func merge(intervals [][]int) [][]int {
    return [][]int{}
}
""";

            case "numIslands" -> """
func numIslands(grid [][]byte) int {
    return 0
}
""";

            case "canFinish" -> """
func canFinish(numCourses int, prerequisites [][]int) bool {
    return false
}
""";

            case "trap" -> """
func trap(height []int) int {
    return 0
}
""";

            default -> """
func solve() {
}
""";
        };
    }

    private String rustStarterCode(
            CodingProblem problem
    ) {

        return switch (problem.getFunctionName()) {

            case "twoSum" -> """
fn two_sum(nums: Vec<i32>, target: i32) -> Vec<i32> {
    vec![]
}
""";

            case "isValid" -> """
fn is_valid(s: String) -> bool {
    false
}
""";

            case "maxProfit" -> """
fn max_profit(prices: Vec<i32>) -> i32 {
    0
}
""";

            case "search" -> """
fn search(nums: Vec<i32>, target: i32) -> i32 {
    -1
}
""";

            case "lengthOfLongestSubstring" -> """
fn length_of_longest_substring(s: String) -> i32 {
    0
}
""";

            case "productExceptSelf" -> """
fn product_except_self(nums: Vec<i32>) -> Vec<i32> {
    vec![]
}
""";

            case "merge" -> """
fn merge(intervals: Vec<Vec<i32>>) -> Vec<Vec<i32>> {
    vec![]
}
""";

            case "numIslands" -> """
fn num_islands(grid: Vec<Vec<char>>) -> i32 {
    0
}
""";

            case "canFinish" -> """
fn can_finish(
    num_courses: i32,
    prerequisites: Vec<Vec<i32>>
) -> bool {
    false
}
""";

            case "trap" -> """
fn trap(height: Vec<i32>) -> i32 {
    0
}
""";

            default -> """
fn solve() {
}
""";
        };
    }

    private String writeJson(
            Object value
    ) {

        try {

            return objectMapper.writeValueAsString(
                    value
            );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Unable to create coding problem configuration.",
                    exception
            );
        }
    }
}