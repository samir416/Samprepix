package com.aiinterview.backend.config;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CodingTestCaseDataInitializer {

    @Bean
    CommandLineRunner initializeCodingTestCases(
            CodingProblemRepository codingProblemRepository,
            CodingTestCaseRepository codingTestCaseRepository
    ) {

        return args -> {

            List<CodingProblem> problems =
                    codingProblemRepository.findByActiveTrue();

            for (CodingProblem problem : problems) {

                List<CodingTestCase> existing =
                        codingTestCaseRepository
                                .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(
                                        problem
                                );

                if (isTestCasesComplete(existing)) {
                    continue;
                }

                if (!existing.isEmpty()) {
                    for (CodingTestCase testCase : existing) {
                        testCase.setActive(false);
                    }

                    codingTestCaseRepository.saveAll(existing);
                }

                createTestCases(
                        problem,
                        codingTestCaseRepository
                );
            }
        };
    }

    private boolean isTestCasesComplete(
            List<CodingTestCase> existing
    ) {

        if (existing == null || existing.size() != 4) {
            return false;
        }

        for (int i = 0; i < existing.size(); i++) {

            CodingTestCase testCase =
                    existing.get(i);

            if (testCase == null ||
                    testCase.getTestCaseNumber() == null ||
                    testCase.getTestCaseNumber() != i + 1 ||
                    testCase.getInput() == null ||
                    testCase.getExpectedOutput() == null) {

                return false;
            }
        }

        return true;
    }

    private void createTestCases(
            CodingProblem problem,
            CodingTestCaseRepository repository
    ) {

        String title =
                problem.getTitle() == null
                        ? ""
                        : problem.getTitle()
                                .trim()
                                .toLowerCase();

        List<CodingTestCase> testCases =
                new ArrayList<>();

        if (title.contains("two sum")) {

            testCases.add(testCase(
                    problem, 1,
                    "2 7 11 15\n9",
                    "0 1",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "3 2 4\n6",
                    "1 2",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "3 3\n6",
                    "0 1",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "1 5 3 7 9\n10",
                    "1 3",
                    true
            ));
        }

        else if (title.contains("valid parentheses")) {

            testCases.add(testCase(
                    problem, 1,
                    "()[]{}",
                    "true",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "(]",
                    "false",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "([{}])",
                    "true",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "(((",
                    "false",
                    true
            ));
        }

        else if (title.contains("best time")) {

            testCases.add(testCase(
                    problem, 1,
                    "7 1 5 3 6 4",
                    "5",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "7 6 4 3 1",
                    "0",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "1 2",
                    "1",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "2 4 1 7 3 8",
                    "7",
                    true
            ));
        }

        else if (title.contains("binary search")) {

            testCases.add(testCase(
                    problem, 1,
                    "-1 0 3 5 9 12\n9",
                    "4",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "-1 0 3 5 9 12\n2",
                    "-1",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "5\n5",
                    "0",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "1 3 5 7 9 11 13\n13",
                    "6",
                    true
            ));
        }

        else if (title.contains("longest substring")) {

            testCases.add(testCase(
                    problem, 1,
                    "abcabcbb",
                    "3",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "bbbbb",
                    "1",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "pwwkew",
                    "3",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "dvdf",
                    "3",
                    true
            ));
        }

        else if (title.contains("product of array")) {

            testCases.add(testCase(
                    problem, 1,
                    "1 2 3 4",
                    "24 12 8 6",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "-1 1 0 -3 3",
                    "0 0 9 0 0",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "2 3 4 5",
                    "60 40 30 24",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "1 0 3 4",
                    "0 12 0 0",
                    true
            ));
        }

        else if (title.contains("merge intervals")) {

            testCases.add(testCase(
                    problem, 1,
                    "1 3\n2 6\n8 10\n15 18",
                    "1 6\n8 10\n15 18",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "1 4\n4 5",
                    "1 5",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "1 2\n3 4\n5 6",
                    "1 2\n3 4\n5 6",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "1 10\n2 3\n4 8\n9 12",
                    "1 12",
                    true
            ));
        }

        else if (title.contains("number of islands")) {

            testCases.add(testCase(
                    problem, 1,
                    "110\n100\n001",
                    "2",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "11110\n11010\n11000\n00000",
                    "1",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "11000\n11000\n00100\n00011",
                    "3",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "10101\n01010\n10101",
                    "5",
                    true
            ));
        }

        else if (title.contains("course schedule")) {

            testCases.add(testCase(
                    problem, 1,
                    "2\n1 0",
                    "true",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "2\n1 0\n0 1",
                    "false",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "3\n1 0\n2 1",
                    "true",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "4\n1 0\n2 1\n3 2\n1 3",
                    "false",
                    true
            ));
        }

        else if (title.contains("trapping rain")) {

            testCases.add(testCase(
                    problem, 1,
                    "0 1 0 2 1 0 1 3 2 1 2 1",
                    "6",
                    false
            ));

            testCases.add(testCase(
                    problem, 2,
                    "4 2 0 3 2 5",
                    "9",
                    false
            ));

            testCases.add(testCase(
                    problem, 3,
                    "1 2 3 4",
                    "0",
                    false
            ));

            testCases.add(testCase(
                    problem, 4,
                    "5 0 0 0 5",
                    "15",
                    true
            ));
        }

        if (!testCases.isEmpty()) {
            repository.saveAll(testCases);
        }
    }

    private CodingTestCase testCase(
            CodingProblem problem,
            int number,
            String input,
            String expectedOutput,
            boolean hidden
    ) {

        return CodingTestCase.builder()
                .problem(problem)
                .testCaseNumber(number)
                .input(input)
                .expectedOutput(expectedOutput)
                .hidden(hidden)
                .active(true)
                .build();
    }
}