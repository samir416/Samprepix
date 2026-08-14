package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingProgress;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.CodingProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@Transactional
public class CodingProgressServiceImpl
        implements CodingProgressService {

    private final CodingProgressRepository codingProgressRepository;

    public CodingProgressServiceImpl(
            CodingProgressRepository codingProgressRepository
    ) {
        this.codingProgressRepository =
                codingProgressRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CodingProgress getProgress(User user) {

        return codingProgressRepository
                .findByUser(user)
                .orElse(null);
    }

    @Override
    public CodingProgress getOrCreateProgress(User user) {

        return codingProgressRepository
                .findByUser(user)
                .orElseGet(() -> {

                    CodingProgress progress =
                            CodingProgress.builder()
                                    .user(user)
                                    .completedProblemIds(
                                            new HashSet<>()
                                    )
                                    .completedProblems(0)
                                    .totalSubmissions(0)
                                    .successfulSubmissions(0)
                                    .started(false)
                                    .completed(false)
                                    .build();

                    return codingProgressRepository.save(
                            progress
                    );
                });
    }

    @Override
    public CodingProgress saveCurrentProblem(
            User user,
            CodingProblem problem
    ) {

        CodingProgress progress =
                getOrCreateProgress(user);

        progress.setCurrentProblem(problem);
        progress.setLastSelectedProblem(problem);

        if (!progress.isStarted()) {

            progress.setStarted(true);
            progress.setStartedAt(
                    LocalDateTime.now()
            );
        }

        progress.setLastActivityAt(
                LocalDateTime.now()
        );

        return codingProgressRepository.save(
                progress
        );
    }

    @Override
    public CodingProgress saveLastSelectedProblem(
            User user,
            CodingProblem problem
    ) {

        CodingProgress progress =
                getOrCreateProgress(user);

        progress.setLastSelectedProblem(problem);
        progress.setLastActivityAt(
                LocalDateTime.now()
        );

        return codingProgressRepository.save(
                progress
        );
    }

    @Override
    public CodingProgress saveCodeState(
            User user,
            CodingProblem problem,
            String language,
            String code
    ) {

        CodingProgress progress =
                getOrCreateProgress(user);

        progress.setCurrentProblem(problem);
        progress.setLastSelectedProblem(problem);
        progress.setLastLanguage(language);
        progress.setLastCode(code);
        progress.setLastActivityAt(
                LocalDateTime.now()
        );

        if (!progress.isStarted()) {

            progress.setStarted(true);
            progress.setStartedAt(
                    LocalDateTime.now()
            );
        }

        return codingProgressRepository.save(
                progress
        );
    }

    @Override
    public CodingProgress markProblemCompleted(
            User user,
            CodingProblem problem
    ) {

        CodingProgress progress =
                getOrCreateProgress(user);

        if (progress.getCompletedProblemIds() == null) {

            progress.setCompletedProblemIds(
                    new HashSet<>()
            );
        }

        boolean newlyCompleted =
                progress.getCompletedProblemIds()
                        .add(problem.getId());

        if (newlyCompleted) {

            progress.setCompletedProblems(
                    progress.getCompletedProblemIds()
                            .size()
            );
        }

        progress.setLastSelectedProblem(problem);
        progress.setLastActivityAt(
                LocalDateTime.now()
        );

        return codingProgressRepository.save(
                progress
        );
    }

    @Override
    public CodingProgress updateSubmission(
            User user,
            boolean successful
    ) {

        CodingProgress progress =
                getOrCreateProgress(user);

        Integer total =
                progress.getTotalSubmissions();

        if (total == null) {
            total = 0;
        }

        progress.setTotalSubmissions(
                total + 1
        );

        if (successful) {

            Integer successfulCount =
                    progress.getSuccessfulSubmissions();

            if (successfulCount == null) {
                successfulCount = 0;
            }

            progress.setSuccessfulSubmissions(
                    successfulCount + 1
            );
        }

        progress.setLastActivityAt(
                LocalDateTime.now()
        );

        return codingProgressRepository.save(
                progress
        );
    }
}