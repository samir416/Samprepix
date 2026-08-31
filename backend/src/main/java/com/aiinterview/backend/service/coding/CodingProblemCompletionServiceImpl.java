package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingProblemCompletion;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.CodingProblemCompletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CodingProblemCompletionServiceImpl
        implements CodingProblemCompletionService {

    private final CodingProblemCompletionRepository completionRepository;

    public CodingProblemCompletionServiceImpl(
            CodingProblemCompletionRepository completionRepository
    ) {
        this.completionRepository = completionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CodingProblemCompletion getCompletion(
            User user,
            CodingProblem problem
    ) {

        validateUser(user);
        validateProblem(problem);

        return completionRepository
                .findByUserIdAndProblemId(
                        user.getId(),
                        problem.getId()
                )
                .orElse(null);
    }

    @Override
    public CodingProblemCompletion getOrCreateCompletion(
            User user,
            CodingProblem problem
    ) {

        validateUser(user);
        validateProblem(problem);

        return completionRepository
                .findByUserIdAndProblemId(
                        user.getId(),
                        problem.getId()
                )
                .orElseGet(() -> {

                    CodingProblemCompletion completion =
                            CodingProblemCompletion.builder()
                                    .user(user)
                                    .problem(problem)
                                    .completed(false)
                                    .submissionCount(0)
                                    .successfulSubmissionCount(0)
                                    .language(null)
                                    .submittedCode(null)
                                    .build();

                    return completionRepository.save(
                            completion
                    );
                });
    }

    @Override
    public CodingProblemCompletion recordSubmission(
            User user,
            CodingProblem problem,
            String language,
            String code,
            boolean successful
    ) {

        validateUser(user);
        validateProblem(problem);

        CodingProblemCompletion completion =
                getOrCreateCompletion(
                        user,
                        problem
                );

        LocalDateTime now =
                LocalDateTime.now();

        int submissionCount =
                completion.getSubmissionCount() == null
                        ? 0
                        : completion.getSubmissionCount();

        completion.setSubmissionCount(
                submissionCount + 1
        );

        completion.setLanguage(
                normalizeLanguage(language)
        );

        completion.setSubmittedCode(
                code
        );

        if (completion.getFirstAttemptAt() == null) {

            completion.setFirstAttemptAt(
                    now
            );
        }

        completion.setLastAttemptAt(
                now
        );

        if (successful) {

            int successfulSubmissionCount =
                    completion
                            .getSuccessfulSubmissionCount() == null
                            ? 0
                            : completion
                                    .getSuccessfulSubmissionCount();

            completion.setSuccessfulSubmissionCount(
                    successfulSubmissionCount + 1
            );

            if (!completion.isCompleted()) {

                completion.setCompleted(
                        true
                );

                completion.setCompletedAt(
                        now
                );
            }
        }

        return completionRepository.save(
                completion
        );
    }

    @Override
    public CodingProblemCompletion markCompleted(
            User user,
            CodingProblem problem,
            String language,
            String code
    ) {

        validateUser(user);
        validateProblem(problem);

        CodingProblemCompletion completion =
                getOrCreateCompletion(
                        user,
                        problem
                );

        LocalDateTime now =
                LocalDateTime.now();

        completion.setLanguage(
                normalizeLanguage(language)
        );

        completion.setSubmittedCode(
                code
        );

        if (completion.getFirstAttemptAt() == null) {

            completion.setFirstAttemptAt(
                    now
            );
        }

        completion.setLastAttemptAt(
                now
        );

        if (!completion.isCompleted()) {

            completion.setCompleted(
                    true
            );

            completion.setCompletedAt(
                    now
            );
        }

        return completionRepository.save(
                completion
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingProblemCompletion> getUserCompletions(
            User user
    ) {

        validateUser(user);

        return completionRepository
                .findByUserIdOrderByCompletedAtAsc(
                        user.getId()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodingProblemCompletion> getCompletedProblems(
            User user
    ) {

        validateUser(user);

        return completionRepository
                .findByUserIdAndCompletedTrueOrderByCompletedAtAsc(
                        user.getId()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProblemCompleted(
            User user,
            CodingProblem problem
    ) {

        validateUser(user);
        validateProblem(problem);

        return completionRepository
                .existsByUserIdAndProblemIdAndCompletedTrue(
                        user.getId(),
                        problem.getId()
                );
    }

    @Override
    @Transactional(readOnly = true)
    public long getCompletedProblemCount(
            User user
    ) {

        validateUser(user);

        return completionRepository
                .countByUserIdAndCompletedTrue(
                        user.getId()
                );
    }

    private String normalizeLanguage(
            String language
    ) {

        if (language == null) {
            return null;
        }

        String normalized =
                language.trim();

        return normalized.isBlank()
                ? null
                : normalized;
    }

    private void validateUser(
            User user
    ) {

        if (
                user == null ||
                user.getId() == null
        ) {

            throw new IllegalArgumentException(
                    "User is required."
            );
        }
    }

    private void validateProblem(
            CodingProblem problem
    ) {

        if (
                problem == null ||
                problem.getId() == null
        ) {

            throw new IllegalArgumentException(
                    "Coding problem is required."
            );
        }
    }
}