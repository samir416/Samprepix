package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.dto.coding.CodingDashboardStatsDto;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingProblemCompletion;
import com.aiinterview.backend.entity.CodingProgress;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.repository.CodingProblemCompletionRepository;
import com.aiinterview.backend.repository.CodingProgressRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CodingProgressServiceImpl
        implements CodingProgressService {

    private final CodingProgressRepository codingProgressRepository;
    private final CodingProblemCompletionRepository completionRepository;

    public CodingProgressServiceImpl(
            CodingProgressRepository codingProgressRepository,
            CodingProblemCompletionRepository completionRepository
    ) {
        this.codingProgressRepository =
                codingProgressRepository;
        this.completionRepository =
                completionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CodingDashboardStatsDto getDashboardStats(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User is required.");
        }

        CodingProgress progress = codingProgressRepository.findByUser(user).orElse(null);
        List<CodingProblemCompletion> completions = completionRepository.findAllByUserIdWithProblem(user.getId());

        int easySolved = 0;
        int mediumSolved = 0;
        int hardSolved = 0;
        int dsaSolved = 0;
        int sqlSolved = 0;

        Map<String, Integer> langCountMap = new LinkedHashMap<>();
        Set<LocalDate> activeDates = new HashSet<>();
        List<CodingProblemCompletion> completedList = new ArrayList<>();

        for (CodingProblemCompletion c : completions) {
            if (c.getFirstAttemptAt() != null) {
                activeDates.add(c.getFirstAttemptAt().toLocalDate());
            }
            if (c.getLastAttemptAt() != null) {
                activeDates.add(c.getLastAttemptAt().toLocalDate());
            }
            if (c.getCompletedAt() != null) {
                activeDates.add(c.getCompletedAt().toLocalDate());
            }

            if (c.getLanguage() != null && !c.getLanguage().isBlank()) {
                String langKey = c.getLanguage().trim();
                langCountMap.put(langKey, langCountMap.getOrDefault(langKey, 0) + 1);
            }

            if (c.isCompleted()) {
                completedList.add(c);
                CodingProblem p = c.getProblem();
                if (p != null) {
                    String diff = p.getDifficulty() != null ? p.getDifficulty().toUpperCase() : "";
                    if (diff.contains("EASY")) {
                        easySolved++;
                    } else if (diff.contains("MEDIUM")) {
                        mediumSolved++;
                    } else if (diff.contains("HARD")) {
                        hardSolved++;
                    }

                    String cat = p.getCategory() != null ? p.getCategory().toUpperCase() : "DSA";
                    if (cat.contains("SQL") || cat.contains("DATA")) {
                        sqlSolved++;
                    } else {
                        dsaSolved++;
                    }
                }
            }
        }

        if (progress != null && progress.getLastActivityAt() != null) {
            activeDates.add(progress.getLastActivityAt().toLocalDate());
        }

        int currentStreak = calculateStreak(activeDates);

        completedList.sort(Comparator.comparing(
                c -> c.getCompletedAt() != null ? c.getCompletedAt() : LocalDateTime.MIN
        ));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
        List<CodingDashboardStatsDto.TimelinePointDto> timeline = new ArrayList<>();
        Map<String, Integer> dateToCumulativeSolved = new LinkedHashMap<>();
        int runningSolved = 0;
        for (CodingProblemCompletion c : completedList) {
            runningSolved++;
            LocalDateTime at = c.getCompletedAt() != null ? c.getCompletedAt() : c.getLastAttemptAt();
            String dateLabel = at != null ? at.format(fmt) : "Day " + runningSolved;
            dateToCumulativeSolved.put(dateLabel, runningSolved);
        }
        for (Map.Entry<String, Integer> entry : dateToCumulativeSolved.entrySet()) {
            timeline.add(new CodingDashboardStatsDto.TimelinePointDto(entry.getKey(), entry.getValue()));
        }

        List<CodingDashboardStatsDto.LanguageCountDto> langDist = langCountMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .map(e -> new CodingDashboardStatsDto.LanguageCountDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        Pageable top6 = PageRequest.of(0, 6);
        List<CodingProblemCompletion> recentList = completionRepository.findRecentByUserIdWithProblem(user.getId(), top6);
        List<CodingDashboardStatsDto.RecentSubmissionDto> recentSubs = new ArrayList<>();
        for (CodingProblemCompletion rc : recentList) {
            CodingProblem p = rc.getProblem();
            recentSubs.add(CodingDashboardStatsDto.RecentSubmissionDto.builder()
                    .problemId(p != null ? p.getId() : null)
                    .problemTitle(p != null ? p.getTitle() : "Problem")
                    .difficulty(p != null ? p.getDifficulty() : "Easy")
                    .category(p != null ? p.getCategory() : "DSA")
                    .language(rc.getLanguage())
                    .completed(rc.isCompleted())
                    .attemptedAt(rc.getLastAttemptAt() != null ? rc.getLastAttemptAt() : rc.getCompletedAt())
                    .build());
        }

        int totalSubmissions = progress != null && progress.getTotalSubmissions() != null ? progress.getTotalSubmissions() : 0;
        int successfulSubmissions = progress != null && progress.getSuccessfulSubmissions() != null ? progress.getSuccessfulSubmissions() : 0;
        int uniqueSolved = completedList.size();
        int uniqueAttempted = completions.size();

        double acceptanceRate = totalSubmissions > 0
                ? Math.round(((double) successfulSubmissions / totalSubmissions) * 1000.0) / 10.0
                : (uniqueAttempted > 0 ? Math.round(((double) uniqueSolved / uniqueAttempted) * 1000.0) / 10.0 : 0.0);

        long totalAvailable = 6260L;

        return CodingDashboardStatsDto.builder()
                .problemsSolved(uniqueSolved)
                .problemsAttempted(uniqueAttempted)
                .totalSubmissions(totalSubmissions)
                .successfulSubmissions(successfulSubmissions)
                .acceptanceRate(acceptanceRate)
                .easySolved(easySolved)
                .mediumSolved(mediumSolved)
                .hardSolved(hardSolved)
                .dsaSolved(dsaSolved)
                .sqlSolved(sqlSolved)
                .totalAvailableProblems(totalAvailable)
                .currentStreak(currentStreak)
                .timeline(timeline)
                .languageDistribution(langDist)
                .recentSubmissions(recentSubs)
                .build();
    }

    private int calculateStreak(Set<LocalDate> activeDates) {
        if (activeDates == null || activeDates.isEmpty()) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        LocalDate checkDate;
        if (activeDates.contains(today)) {
            checkDate = today;
        } else if (activeDates.contains(yesterday)) {
            checkDate = yesterday;
        } else {
            return 0;
        }

        int streak = 0;
        while (activeDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }
        return streak;
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