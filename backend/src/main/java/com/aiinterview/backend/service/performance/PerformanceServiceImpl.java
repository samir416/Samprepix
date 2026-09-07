package com.aiinterview.backend.service.performance;

import com.aiinterview.backend.dto.performance.PerformanceAnalyticsDto;
import com.aiinterview.backend.entity.*;
import com.aiinterview.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PerformanceServiceImpl implements PerformanceService {

    private final CodingProblemCompletionRepository completionRepository;
    private final CodingProgressRepository codingProgressRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;

    public PerformanceServiceImpl(
            CodingProblemCompletionRepository completionRepository,
            CodingProgressRepository codingProgressRepository,
            InterviewSessionRepository interviewSessionRepository,
            ResumeAnalysisRepository resumeAnalysisRepository
    ) {
        this.completionRepository = completionRepository;
        this.codingProgressRepository = codingProgressRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
    }

    @Override
    public PerformanceAnalyticsDto getPerformanceAnalytics(User user) {
        // 1. Coding Metrics
        List<CodingProblemCompletion> completions = completionRepository.findAllByUserIdWithProblem(user.getId());
        List<CodingProblemCompletion> completedList = completions.stream()
                .filter(CodingProblemCompletion::isCompleted)
                .toList();

        int uniqueSolved = completedList.size();
        int uniqueAttempted = completions.size();

        int easySolved = 0;
        int mediumSolved = 0;
        int hardSolved = 0;
        int dsaSolved = 0;
        int sqlSolved = 0;

        Set<LocalDate> activeDates = new HashSet<>();

        for (CodingProblemCompletion c : completions) {
            if (c.getLastAttemptAt() != null) {
                activeDates.add(c.getLastAttemptAt().toLocalDate());
            }
            if (c.getCompletedAt() != null) {
                activeDates.add(c.getCompletedAt().toLocalDate());
            }

            if (c.isCompleted()) {
                CodingProblem p = c.getProblem();
                if (p != null) {
                    String diff = p.getDifficulty() != null ? p.getDifficulty().toUpperCase() : "EASY";
                    switch (diff) {
                        case "EASY" -> easySolved++;
                        case "MEDIUM" -> mediumSolved++;
                        case "HARD" -> hardSolved++;
                        default -> easySolved++;
                    }

                    String cat = p.getCategory() != null ? p.getCategory().toUpperCase() : "DSA";
                    if ("DATABASE".equals(cat) || "SQL".equals(cat)) {
                        sqlSolved++;
                    } else {
                        dsaSolved++;
                    }
                }
            }
        }

        CodingProgress progress = codingProgressRepository.findByUserId(user.getId()).orElse(null);
        int totalSubmissions = progress != null && progress.getTotalSubmissions() != null ? progress.getTotalSubmissions() : 0;
        int successfulSubmissions = progress != null && progress.getSuccessfulSubmissions() != null ? progress.getSuccessfulSubmissions() : 0;

        double acceptanceRate = totalSubmissions > 0
                ? Math.round(((double) successfulSubmissions / totalSubmissions) * 1000.0) / 10.0
                : (uniqueAttempted > 0 ? Math.round(((double) uniqueSolved / uniqueAttempted) * 1000.0) / 10.0 : 0.0);

        int currentStreak = calculateStreak(activeDates);

        // Timeline
        List<PerformanceAnalyticsDto.TimelinePointDto> timeline = generateTimeline(completedList);

        // Skill Metrics
        List<PerformanceAnalyticsDto.SkillMetricDto> skillMetrics = new ArrayList<>();
        double dsaProg = Math.round(((double) dsaSolved / 5060.0) * 1000.0) / 10.0;
        double sqlProg = Math.round(((double) sqlSolved / 1200.0) * 1000.0) / 10.0;
        double problemSolvingProg = uniqueSolved > 0 ? Math.min(100.0, Math.round(uniqueSolved * 10.0)) : 0.0;

        skillMetrics.add(PerformanceAnalyticsDto.SkillMetricDto.builder()
                .name("DSA (Algorithms & Data Structures)")
                .solved(dsaSolved)
                .total(5060)
                .progress(dsaProg)
                .color("#8b5cf6")
                .build());

        skillMetrics.add(PerformanceAnalyticsDto.SkillMetricDto.builder()
                .name("Database (SQL Queries & Schema)")
                .solved(sqlSolved)
                .total(1200)
                .progress(sqlProg)
                .color("#06b6d4")
                .build());

        skillMetrics.add(PerformanceAnalyticsDto.SkillMetricDto.builder()
                .name("Core Problem Solving")
                .solved(uniqueSolved)
                .total(6260)
                .progress(problemSolvingProg)
                .color("#10b981")
                .build());

        // 2. Mock Interview Metrics
        List<InterviewSession> sessions = interviewSessionRepository.findByUser(user);
        List<InterviewSession> evaluatedSessions = sessions.stream()
                .filter(s -> s.getOverallScore() != null && s.getOverallScore() > 0)
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getCompletedAt() != null ? a.getCompletedAt() : a.getStartedAt();
                    LocalDateTime tb = b.getCompletedAt() != null ? b.getCompletedAt() : b.getStartedAt();
                    return tb.compareTo(ta);
                })
                .toList();

        boolean hasInterviewData = !evaluatedSessions.isEmpty();
        int totalInterviews = evaluatedSessions.size();
        Double avgInterviewScore = null;
        Double avgTechAccuracy = null;
        Double avgCommunication = null;
        Double avgCompleteness = null;
        List<PerformanceAnalyticsDto.RecentInterviewDto> recentInterviews = new ArrayList<>();

        if (hasInterviewData) {
            avgInterviewScore = Math.round(evaluatedSessions.stream().mapToInt(InterviewSession::getOverallScore).average().orElse(0.0) * 10.0) / 10.0;
            avgTechAccuracy = Math.round(evaluatedSessions.stream().filter(s -> s.getTechnicalAccuracy() != null).mapToInt(InterviewSession::getTechnicalAccuracy).average().orElse(0.0) * 10.0) / 10.0;
            avgCommunication = Math.round(evaluatedSessions.stream().filter(s -> s.getCommunication() != null).mapToInt(InterviewSession::getCommunication).average().orElse(0.0) * 10.0) / 10.0;
            avgCompleteness = Math.round(evaluatedSessions.stream().filter(s -> s.getCompleteness() != null).mapToInt(InterviewSession::getCompleteness).average().orElse(0.0) * 10.0) / 10.0;

            for (InterviewSession s : evaluatedSessions.stream().limit(5).toList()) {
                recentInterviews.add(PerformanceAnalyticsDto.RecentInterviewDto.builder()
                        .id(s.getId())
                        .targetRole(s.getTargetRole())
                        .interviewType(s.getInterviewType())
                        .overallScore(s.getOverallScore())
                        .technicalAccuracy(s.getTechnicalAccuracy())
                        .communication(s.getCommunication())
                        .completedAt(s.getCompletedAt() != null ? s.getCompletedAt() : s.getStartedAt())
                        .build());
            }
        }

        // 3. Resume ATS Metrics
        ResumeAnalysis resume = resumeAnalysisRepository.findTopByUserEmailOrderByIdDesc(user.getEmail());
        boolean hasResumeData = resume != null && resume.getScore() > 0;
        Integer latestResumeScore = hasResumeData ? resume.getScore() : null;
        String resumeAnalyzedAt = hasResumeData ? resume.getAnalyzedAt() : null;

        // 4. Composite Placement Readiness Score
        double compositeScore = 0.0;
        double totalWeight = 0.0;

        // Coding contribution (normalized to 100)
        double codingScore = Math.min(100.0, uniqueSolved * 15.0 + (acceptanceRate * 0.4));
        if (uniqueAttempted > 0) {
            compositeScore += codingScore * 0.35;
            totalWeight += 0.35;
        }

        if (hasInterviewData && avgInterviewScore != null) {
            compositeScore += avgInterviewScore * 0.40;
            totalWeight += 0.40;
        }

        if (hasResumeData && latestResumeScore != null) {
            compositeScore += latestResumeScore * 0.25;
            totalWeight += 0.25;
        }

        int finalReadiness = totalWeight > 0 ? (int) Math.round(compositeScore / totalWeight) : 0;
        String readinessStatus;
        if (finalReadiness >= 85) {
            readinessStatus = "Placement Ready";
        } else if (finalReadiness >= 65) {
            readinessStatus = "Strong Candidate";
        } else if (finalReadiness >= 40) {
            readinessStatus = "Developing";
        } else if (finalReadiness > 0) {
            readinessStatus = "Needs Practice";
        } else {
            readinessStatus = "Not Started";
        }

        // 5. Recent Combined Activities
        List<PerformanceAnalyticsDto.ActivityLogDto> activities = new ArrayList<>();

        for (CodingProblemCompletion c : completions.stream()
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getLastAttemptAt() != null ? a.getLastAttemptAt() : a.getCompletedAt();
                    LocalDateTime tb = b.getLastAttemptAt() != null ? b.getLastAttemptAt() : b.getCompletedAt();
                    if (ta == null && tb == null) return 0;
                    if (ta == null) return 1;
                    if (tb == null) return -1;
                    return tb.compareTo(ta);
                })
                .limit(5)
                .toList()) {
            LocalDateTime ts = c.getLastAttemptAt() != null ? c.getLastAttemptAt() : c.getCompletedAt();
            String timeStr = ts != null ? ts.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "Recently";
            activities.add(PerformanceAnalyticsDto.ActivityLogDto.builder()
                    .type("CODING")
                    .title(c.getProblem() != null ? c.getProblem().getTitle() : "Problem")
                    .status(c.isCompleted() ? "ACCEPTED" : "ATTEMPTED")
                    .score(c.isCompleted() ? "100%" : "Failed")
                    .timestamp(timeStr)
                    .build());
        }

        if (hasInterviewData) {
            for (InterviewSession s : evaluatedSessions.stream().limit(5).toList()) {
                LocalDateTime ts = s.getCompletedAt() != null ? s.getCompletedAt() : s.getStartedAt();
                String timeStr = ts != null ? ts.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "Recently";
                activities.add(PerformanceAnalyticsDto.ActivityLogDto.builder()
                        .type("INTERVIEW")
                        .title((s.getTargetRole() != null ? s.getTargetRole() : "Mock") + " Interview")
                        .status(s.getOverallScore() >= 70 ? "PASSED" : "COMPLETED")
                        .score(s.getOverallScore() + "%")
                        .timestamp(timeStr)
                        .build());
            }
        }

        if (hasResumeData) {
            activities.add(PerformanceAnalyticsDto.ActivityLogDto.builder()
                    .type("RESUME")
                    .title("Resume ATS Analysis")
                    .status("ANALYZED")
                    .score(latestResumeScore + "%")
                    .timestamp(resumeAnalyzedAt != null ? resumeAnalyzedAt : "Recently")
                    .build());
        }

        // 6. Dynamic Problem Solving Score derived from authentic Coding Arena user data
        // Weights: Easy=1.0, Medium=2.5, Hard=5.0 with acceptance rate accuracy
        Double dynamicProblemSolvingScore = null;
        if (uniqueAttempted > 0) {
            double weightedPoints = (easySolved * 1.0) + (mediumSolved * 2.5) + (hardSolved * 5.0);
            double volumeComponent = Math.min(60.0, (weightedPoints / 25.0) * 60.0);
            double accuracyComponent = (acceptanceRate / 100.0) * 40.0;
            if (uniqueSolved == 0) {
                dynamicProblemSolvingScore = 0.0;
            } else {
                double raw = volumeComponent + accuracyComponent;
                dynamicProblemSolvingScore = Math.round(Math.min(100.0, Math.max(10.0, raw)) * 10.0) / 10.0;
            }
        }

        return PerformanceAnalyticsDto.builder()
                .placementReadinessScore(finalReadiness)
                .readinessStatus(readinessStatus)
                .technicalScore(avgTechAccuracy != null ? avgTechAccuracy : (uniqueSolved > 0 ? acceptanceRate : null))
                .problemSolvingScore(dynamicProblemSolvingScore)
                .resumeAtsScore(latestResumeScore)
                .problemsSolved(uniqueSolved)
                .problemsAttempted(uniqueAttempted)
                .totalSubmissions(totalSubmissions)
                .acceptanceRate(acceptanceRate)
                .easySolved(easySolved)
                .mediumSolved(mediumSolved)
                .hardSolved(hardSolved)
                .dsaSolved(dsaSolved)
                .sqlSolved(sqlSolved)
                .currentStreak(currentStreak)
                .skillMetrics(skillMetrics)
                .timeline(timeline)
                .hasInterviewData(hasInterviewData)
                .totalInterviews(totalInterviews)
                .avgInterviewScore(avgInterviewScore)
                .avgTechnicalAccuracy(avgTechAccuracy)
                .avgCommunication(avgCommunication)
                .avgCompleteness(avgCompleteness)
                .recentInterviews(recentInterviews)
                .hasResumeData(hasResumeData)
                .latestResumeScore(latestResumeScore)
                .resumeAnalyzedAt(resumeAnalyzedAt)
                .recentActivities(activities)
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

    private List<PerformanceAnalyticsDto.TimelinePointDto> generateTimeline(List<CodingProblemCompletion> completedList) {
        if (completedList == null || completedList.isEmpty()) {
            return Collections.emptyList();
        }

        Map<LocalDate, Integer> dateCounts = new TreeMap<>();
        for (CodingProblemCompletion c : completedList) {
            LocalDate d = c.getCompletedAt() != null ? c.getCompletedAt().toLocalDate() : LocalDate.now();
            dateCounts.put(d, dateCounts.getOrDefault(d, 0) + 1);
        }

        List<PerformanceAnalyticsDto.TimelinePointDto> list = new ArrayList<>();
        int cumulative = 0;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd");

        for (Map.Entry<LocalDate, Integer> entry : dateCounts.entrySet()) {
            cumulative += entry.getValue();
            list.add(PerformanceAnalyticsDto.TimelinePointDto.builder()
                    .date(entry.getKey().format(dtf))
                    .cumulativeSolved(cumulative)
                    .dailySolved(entry.getValue())
                    .build());
        }

        return list;
    }
}
