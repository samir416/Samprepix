package com.aiinterview.backend.config;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.repository.CodingProblemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CodingProblemSeeder {

    @Bean
    CommandLineRunner seedCodingProblems(
            CodingProblemRepository repository
    ) {
        return args -> {

            if (repository.count() > 0) {
                return;
            }

            repository.saveAll(
                    List.of(

                            CodingProblem.builder()
                                    .title("Two Sum")
                                    .description(
                                            "Given an array of integers and a target value, return the indices of the two numbers that add up to the target."
                                    )
                                    .difficulty("EASY")
                                    .tags(List.of(
                                            "Array",
                                            "Hash Table"
                                    ))
                                    .inputExample(
                                            "nums = [2,7,11,15], target = 9"
                                    )
                                    .outputExample(
                                            "[0,1]"
                                    )
                                    .constraints(List.of(
                                            "2 <= nums.length <= 10^4",
                                            "-10^9 <= nums[i] <= 10^9",
                                            "-10^9 <= target <= 10^9"
                                    ))
                                    .starterCode(
                                            "function twoSum(nums, target) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(1)
                                    .active(true)
                                    .build(),

                            CodingProblem.builder()
                                    .title("Valid Parentheses")
                                    .description(
                                            "Given a string containing brackets, determine whether the input string is valid. Every opening bracket must be closed by the same type of bracket in the correct order."
                                    )
                                    .difficulty("EASY")
                                    .tags(List.of(
                                            "String",
                                            "Stack"
                                    ))
                                    .inputExample(
                                            "s = \"()[]{}\""
                                    )
                                    .outputExample(
                                            "true"
                                    )
                                    .constraints(List.of(
                                            "1 <= s.length <= 10^4",
                                            "s consists only of parentheses characters."
                                    ))
                                    .starterCode(
                                            "function isValid(s) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(1)
                                    .active(true)
                                    .build(),

                            CodingProblem.builder()
                                    .title("Best Time to Buy and Sell Stock")
                                    .description(
                                            "Given an array where prices[i] is the price of a stock on day i, find the maximum profit that can be achieved by buying on one day and selling on a later day."
                                    )
                                    .difficulty("EASY")
                                    .tags(List.of(
                                            "Array",
                                            "Greedy"
                                    ))
                                    .inputExample(
                                            "prices = [7,1,5,3,6,4]"
                                    )
                                    .outputExample(
                                            "5"
                                    )
                                    .constraints(List.of(
                                            "1 <= prices.length <= 10^5",
                                            "0 <= prices[i] <= 10^4"
                                    ))
                                    .starterCode(
                                            "function maxProfit(prices) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(1)
                                    .active(true)
                                    .build(),

                            CodingProblem.builder()
                                    .title("Binary Search")
                                    .description(
                                            "Given a sorted array of integers and a target value, return the index of the target if it exists. Otherwise return -1."
                                    )
                                    .difficulty("MEDIUM")
                                    .tags(List.of(
                                            "Array",
                                            "Binary Search"
                                    ))
                                    .inputExample(
                                            "nums = [-1,0,3,5,9,12], target = 9"
                                    )
                                    .outputExample(
                                            "4"
                                    )
                                    .constraints(List.of(
                                            "1 <= nums.length <= 10^4",
                                            "-10^4 <= nums[i], target <= 10^4",
                                            "All integers in nums are unique.",
                                            "nums is sorted in ascending order."
                                    ))
                                    .starterCode(
                                            "function search(nums, target) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(2)
                                    .active(true)
                                    .build(),

                            CodingProblem.builder()
                                    .title("Longest Substring Without Repeating Characters")
                                    .description(
                                            "Given a string, find the length of the longest substring without repeating characters."
                                    )
                                    .difficulty("MEDIUM")
                                    .tags(List.of(
                                            "String",
                                            "Sliding Window",
                                            "Hash Table"
                                    ))
                                    .inputExample(
                                            "s = \"abcabcbb\""
                                    )
                                    .outputExample(
                                            "3"
                                    )
                                    .constraints(List.of(
                                            "0 <= s.length <= 5 * 10^4",
                                            "s consists of English letters, digits, symbols and spaces."
                                    ))
                                    .starterCode(
                                            "function lengthOfLongestSubstring(s) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(2)
                                    .active(true)
                                    .build(),

                            CodingProblem.builder()
                                    .title("Product of Array Except Self")
                                    .description(
                                            "Given an integer array, return an array where each element is the product of all elements except the element at the same index."
                                    )
                                    .difficulty("MEDIUM")
                                    .tags(List.of(
                                            "Array",
                                            "Prefix Sum"
                                    ))
                                    .inputExample(
                                            "nums = [1,2,3,4]"
                                    )
                                    .outputExample(
                                            "[24,12,8,6]"
                                    )
                                    .constraints(List.of(
                                            "2 <= nums.length <= 10^5",
                                            "-30 <= nums[i] <= 30",
                                            "The product of any prefix or suffix fits in a 32-bit integer."
                                    ))
                                    .starterCode(
                                            "function productExceptSelf(nums) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(2)
                                    .active(true)
                                    .build(),

                            CodingProblem.builder()
                                    .title("Merge Intervals")
                                    .description(
                                            "Given an array of intervals, merge all overlapping intervals and return an array of the non-overlapping intervals that cover all the input intervals."
                                    )
                                    .difficulty("MEDIUM")
                                    .tags(List.of(
                                            "Array",
                                            "Sorting",
                                            "Intervals"
                                    ))
                                    .inputExample(
                                            "intervals = [[1,3],[2,6],[8,10],[15,18]]"
                                    )
                                    .outputExample(
                                            "[[1,6],[8,10],[15,18]]"
                                    )
                                    .constraints(List.of(
                                            "1 <= intervals.length <= 10^4",
                                            "intervals[i].length == 2",
                                            "0 <= start <= end <= 10^4"
                                    ))
                                    .starterCode(
                                            "function merge(intervals) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(2)
                                    .active(true)
                                    .build(),

                            CodingProblem.builder()
                                    .title("Number of Islands")
                                    .description(
                                            "Given a grid of land and water cells, count the number of islands. An island is formed by connecting adjacent land cells horizontally or vertically."
                                    )
                                    .difficulty("HARD")
                                    .tags(List.of(
                                            "Graph",
                                            "DFS",
                                            "BFS",
                                            "Matrix"
                                    ))
                                    .inputExample(
                                            "grid = [[\"1\",\"1\",\"0\"],[\"1\",\"0\",\"0\"],[\"0\",\"0\",\"1\"]]"
                                    )
                                    .outputExample(
                                            "2"
                                    )
                                    .constraints(List.of(
                                            "1 <= rows, columns <= 300",
                                            "grid[i][j] is either '0' or '1'."
                                    ))
                                    .starterCode(
                                            "function numIslands(grid) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(3)
                                    .active(true)
                                    .build(),

                            CodingProblem.builder()
                                    .title("Course Schedule")
                                    .description(
                                            "There are a number of courses to take. Some courses have prerequisites. Determine whether it is possible to finish all courses."
                                    )
                                    .difficulty("HARD")
                                    .tags(List.of(
                                            "Graph",
                                            "BFS",
                                            "DFS",
                                            "Topological Sort"
                                    ))
                                    .inputExample(
                                            "numCourses = 2, prerequisites = [[1,0]]"
                                    )
                                    .outputExample(
                                            "true"
                                    )
                                    .constraints(List.of(
                                            "1 <= numCourses <= 2000",
                                            "0 <= prerequisites.length <= 5000",
                                            "prerequisites[i].length == 2"
                                    ))
                                    .starterCode(
                                            "function canFinish(numCourses, prerequisites) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(3)
                                    .active(true)
                                    .build(),

                            CodingProblem.builder()
                                    .title("Trapping Rain Water")
                                    .description(
                                            "Given an array representing elevation heights, calculate how much rain water can be trapped after raining."
                                    )
                                    .difficulty("HARD")
                                    .tags(List.of(
                                            "Array",
                                            "Two Pointers",
                                            "Stack"
                                    ))
                                    .inputExample(
                                            "height = [0,1,0,2,1,0,1,3,2,1,2,1]"
                                    )
                                    .outputExample(
                                            "6"
                                    )
                                    .constraints(List.of(
                                            "1 <= height.length <= 2 * 10^4",
                                            "0 <= height[i] <= 10^5"
                                    ))
                                    .starterCode(
                                            "function trap(height) {\n    \n}"
                                    )
                                    .minimumExperienceLevel(3)
                                    .active(true)
                                    .build()

                    )
            );
        };
    }
}