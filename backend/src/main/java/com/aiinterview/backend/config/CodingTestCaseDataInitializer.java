package com.aiinterview.backend.config;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                    codingProblemRepository
                            .findByActiveTrue();

            for (CodingProblem problem : problems) {

                if (codingTestCaseRepository
                        .findByProblemAndActiveTrueOrderByTestCaseNumberAsc(
                                problem
                        )
                        .isEmpty()) {

                    createTestCases(
                            problem,
                            codingTestCaseRepository
                    );
                }
            }
        };
    }

    private void createTestCases(
            CodingProblem problem,
            CodingTestCaseRepository repository
    ) {

        String title =
                problem.getTitle()
                        .trim()
                        .toLowerCase();

        if (title.contains("two sum")) {

            repository.saveAll(
                    List.of(
                            testCase(
                                    problem,
                                    1,
                                    "2 7 11 15",
                                    "0 1",
                                    false
                            ),
                            testCase(
                                    problem,
                                    2,
                                    "3 2 4",
                                    "1 2",
                                    false
                            ),
                            testCase(
                                    problem,
                                    3,
                                    "3 3",
                                    "0 1",
                                    true
                            )
                    )
            );

            return;
        }

        if (title.contains("reverse string")) {

            repository.saveAll(
                    List.of(
                            testCase(
                                    problem,
                                    1,
                                    "hello",
                                    "olleh",
                                    false
                            ),
                            testCase(
                                    problem,
                                    2,
                                    "world",
                                    "dlrow",
                                    false
                            ),
                            testCase(
                                    problem,
                                    3,
                                    "coding",
                                    "gnidoc",
                                    true
                            )
                    )
            );

            return;
        }

        if (title.contains("palindrome")) {

            repository.saveAll(
                    List.of(
                            testCase(
                                    problem,
                                    1,
                                    "madam",
                                    "true",
                                    false
                            ),
                            testCase(
                                    problem,
                                    2,
                                    "hello",
                                    "false",
                                    false
                            ),
                            testCase(
                                    problem,
                                    3,
                                    "racecar",
                                    "true",
                                    true
                            )
                    )
            );

            return;
        }

        if (problem.getInputExample() != null &&
                problem.getOutputExample() != null) {

            repository.save(
                    testCase(
                            problem,
                            1,
                            problem.getInputExample(),
                            problem.getOutputExample(),
                            false
                    )
            );
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