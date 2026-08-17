package com.aiinterview.backend.config;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class CodingProblemSeeder {

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Bean
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

            List<CodingProblem> problems =
                    repository.findAll();

            boolean changed = false;

            for (CodingProblem problem :
                    problems) {

                if (problem.getLanguageConfigurations() ==
                        null ||
                        problem.getLanguageConfigurations()
                                .isBlank() ||
                        "{}".equals(
                                problem.getLanguageConfigurations()
                        )) {

                    applyProblemConfiguration(
                            problem
                    );

                    changed = true;
                }

                if (problem.getFunctionName() ==
                        null ||
                        problem.getFunctionName().isBlank()) {

                    applyFunctionMetadata(
                            problem
                    );

                    changed = true;
                }
            }

            if (changed) {

                repository.saveAll(
                        problems
                );
            }
        };
    }

    private CodingProblem twoSum() {

        return base(
                "Two Sum",
                "Given an array of integers and a target value, return the indices of the two numbers that add up to the target.",
                "EASY",
                List.of(
                        "Array",
                        "Hash Table"
                ),
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
                List.of(
                        "String",
                        "Stack"
                ),
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
                List.of(
                        "Array",
                        "Greedy"
                ),
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
                List.of(
                        "Array",
                        "Binary Search"
                ),
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
                List.of(
                        "String",
                        "Sliding Window",
                        "Hash Table"
                ),
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
                List.of(
                        "Array",
                        "Prefix Sum"
                ),
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
                List.of(
                        "Array",
                        "Sorting",
                        "Intervals"
                ),
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
                List.of(
                        "Graph",
                        "DFS",
                        "BFS",
                        "Matrix"
                ),
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
                List.of(
                        "Graph",
                        "BFS",
                        "DFS",
                        "Topological Sort"
                ),
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
                List.of(
                        "Array",
                        "Two Pointers",
                        "Stack"
                ),
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
                        .functionSignature(
                                functionSignature
                        )
                        .returnType(returnType)
                        .parameterTypes(
                                parameterTypes
                        )
                        .starterCode("")
                        .minimumExperienceLevel(
                                experience
                        )
                        .active(true)
                        .build();

        applyProblemConfiguration(
                problem
        );

        return problem;
    }

    private void applyFunctionMetadata(
            CodingProblem problem
    ) {

        String title =
                problem.getTitle()
                        .trim()
                        .toLowerCase();

        if (title.contains("two sum")) {

            problem.setFunctionName(
                    "twoSum"
            );

            problem.setFunctionSignature(
                    "twoSum(nums, target)"
            );

            problem.setReturnType(
                    "array"
            );

            problem.setParameterTypes(
                    "array,integer"
            );

            return;
        }

        if (title.contains(
                "valid parentheses"
        )) {

            problem.setFunctionName(
                    "isValid"
            );

            problem.setFunctionSignature(
                    "isValid(s)"
            );

            problem.setReturnType(
                    "boolean"
            );

            problem.setParameterTypes(
                    "string"
            );

            return;
        }

        if (title.contains(
                "best time"
        )) {

            problem.setFunctionName(
                    "maxProfit"
            );

            problem.setFunctionSignature(
                    "maxProfit(prices)"
            );

            problem.setReturnType(
                    "integer"
            );

            problem.setParameterTypes(
                    "integer[]"
            );

            return;
        }

        if (title.contains(
                "binary search"
        )) {

            problem.setFunctionName(
                    "search"
            );

            problem.setFunctionSignature(
                    "search(nums, target)"
            );

            problem.setReturnType(
                    "integer"
            );

            problem.setParameterTypes(
                    "integer[],integer"
            );

            return;
        }

        if (title.contains(
                "longest substring"
        )) {

            problem.setFunctionName(
                    "lengthOfLongestSubstring"
            );

            problem.setFunctionSignature(
                    "lengthOfLongestSubstring(s)"
            );

            problem.setReturnType(
                    "integer"
            );

            problem.setParameterTypes(
                    "string"
            );

            return;
        }

        if (title.contains(
                "product of array"
        )) {

            problem.setFunctionName(
                    "productExceptSelf"
            );

            problem.setFunctionSignature(
                    "productExceptSelf(nums)"
            );

            problem.setReturnType(
                    "integer[]"
            );

            problem.setParameterTypes(
                    "integer[]"
            );

            return;
        }

        if (title.contains(
                "merge intervals"
        )) {

            problem.setFunctionName(
                    "merge"
            );

            problem.setFunctionSignature(
                    "merge(intervals)"
            );

            problem.setReturnType(
                    "integer[][]"
            );

            problem.setParameterTypes(
                    "integer[][]"
            );

            return;
        }

        if (title.contains(
                "number of islands"
        )) {

            problem.setFunctionName(
                    "numIslands"
            );

            problem.setFunctionSignature(
                    "numIslands(grid)"
            );

            problem.setReturnType(
                    "integer"
            );

            problem.setParameterTypes(
                    "character[][]"
            );

            return;
        }

        if (title.contains(
                "course schedule"
        )) {

            problem.setFunctionName(
                    "canFinish"
            );

            problem.setFunctionSignature(
                    "canFinish(numCourses, prerequisites)"
            );

            problem.setReturnType(
                    "boolean"
            );

            problem.setParameterTypes(
                    "integer,integer[][]"
            );

            return;
        }

        if (title.contains(
                "trapping rain"
        )) {

            problem.setFunctionName(
                    "trap"
            );

            problem.setFunctionSignature(
                    "trap(height)"
            );

            problem.setReturnType(
                    "integer"
            );

            problem.setParameterTypes(
                    "integer[]"
            );
        }
    }

    private void applyProblemConfiguration(
            CodingProblem problem
    ) {

        Map<String, Object> configurations =
                new LinkedHashMap<>();

        problem.setLanguageConfigurations(
                writeJson(
                        configurations
                )
        );

        problem.setStarterCode(
                ""
        );
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