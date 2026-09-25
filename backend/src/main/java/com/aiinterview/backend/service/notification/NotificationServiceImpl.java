package com.aiinterview.backend.service.notification;

import com.aiinterview.backend.dto.notification.NotificationDto;
import com.aiinterview.backend.entity.*;
import com.aiinterview.backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final CodingProblemCompletionRepository completionRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final AptitudeAttemptRepository aptitudeAttemptRepository;
    private final UserRepository userRepository;
    private final GithubAnalysisResultRepository githubAnalysisResultRepository;
    private final UserRoadmapRepository userRoadmapRepository;
    private final SubscriptionRepository subscriptionRepository;

    public NotificationServiceImpl(
            CodingProblemCompletionRepository completionRepository,
            InterviewSessionRepository interviewSessionRepository,
            ResumeAnalysisRepository resumeAnalysisRepository,
            AptitudeAttemptRepository aptitudeAttemptRepository,
            UserRepository userRepository,
            GithubAnalysisResultRepository githubAnalysisResultRepository,
            UserRoadmapRepository userRoadmapRepository,
            SubscriptionRepository subscriptionRepository
    ) {
        this.completionRepository = completionRepository;
        this.interviewSessionRepository = interviewSessionRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.aptitudeAttemptRepository = aptitudeAttemptRepository;
        this.userRepository = userRepository;
        this.githubAnalysisResultRepository = githubAnalysisResultRepository;
        this.userRoadmapRepository = userRoadmapRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(User user) {
        LocalDateTime lastRead = user.getLastNotificationsReadAt();
        LocalDateTime dismissedAllBefore = user.getDismissedAllNotificationsBefore();
        Set<String> dismissedIds = user.getDismissedNotificationIds();
        if (dismissedIds == null) {
            dismissedIds = Collections.emptySet();
        }

        List<NotificationDto> list = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. Coding Problem Completions & Attempts
        List<CodingProblemCompletion> completions = completionRepository.findAllByUserIdWithProblem(user.getId());
        for (CodingProblemCompletion c : completions) {
            LocalDateTime ts = c.getCompletedAt() != null ? c.getCompletedAt() : c.getLastAttemptAt();
            if (ts == null) {
                ts = now.minusHours(1);
            }

            boolean isCompleted = c.isCompleted();
            String id = (isCompleted ? "coding-comp-" : "coding-att-") + c.getId();

            if ((dismissedAllBefore != null && !ts.isAfter(dismissedAllBefore)) || dismissedIds.contains(id)) {
                continue;
            }

            String title = c.getProblem() != null ? c.getProblem().getTitle() : "Problem";
            String diff = c.getProblem() != null && c.getProblem().getDifficulty() != null
                    ? c.getProblem().getDifficulty() : "Standard";

            String notifTitle = isCompleted ? "Problem Solved: " + title : "Problem Attempted: " + title;
            String message = isCompleted
                    ? "Passed all test cases for \"" + title + "\" (" + diff + ") in Coding Arena."
                    : "Recorded your test case submission for \"" + title + "\".";

            boolean unread = lastRead == null || ts.isAfter(lastRead);

            list.add(NotificationDto.builder()
                    .id(id)
                    .type("CODING")
                    .title(notifTitle)
                    .message(message)
                    .timestamp(formatRelativeTime(ts, now))
                    .createdAt(ts)
                    .unread(unread)
                    .targetUrl("/coding-arena")
                    .build());
        }

        // 2. Mock Interview Sessions
        List<InterviewSession> sessions = interviewSessionRepository.findByUser(user);
        for (InterviewSession s : sessions) {
            if (s.getOverallScore() != null && s.getOverallScore() > 0) {
                LocalDateTime ts = s.getCompletedAt() != null ? s.getCompletedAt() : s.getStartedAt();
                if (ts == null) {
                    ts = now.minusHours(2);
                }

                String id = "interview-" + s.getId();
                if ((dismissedAllBefore != null && !ts.isAfter(dismissedAllBefore)) || dismissedIds.contains(id)) {
                    continue;
                }

                String role = s.getTargetRole() != null ? s.getTargetRole() : "Technical";
                boolean unread = lastRead == null || ts.isAfter(lastRead);

                list.add(NotificationDto.builder()
                        .id(id)
                        .type("INTERVIEW")
                        .title(role + " Mock Completed")
                        .message("AI Evaluation finalized with an overall score of " + s.getOverallScore() + "%.")
                        .timestamp(formatRelativeTime(ts, now))
                        .createdAt(ts)
                        .unread(unread)
                        .targetUrl("/performance")
                        .build());
            }
        }

        // 3. Resume ATS Analyses
        List<ResumeAnalysis> resumes = resumeAnalysisRepository.findByUserEmail(user.getEmail());
        if (resumes != null) {
            for (ResumeAnalysis r : resumes) {
                if (r.getScore() > 0) {
                    LocalDateTime ts = parseResumeTimestamp(r.getAnalyzedAt(), now);
                    String id = "resume-" + r.getId();

                    if ((dismissedAllBefore != null && !ts.isAfter(dismissedAllBefore)) || dismissedIds.contains(id)) {
                        continue;
                    }

                    boolean unread = lastRead == null || ts.isAfter(lastRead);

                    list.add(NotificationDto.builder()
                            .id(id)
                            .type("RESUME")
                            .title("Resume ATS Analyzed")
                            .message("Resume ATS score: " + r.getScore() + "%. Actionable keyword improvements are ready.")
                            .timestamp(formatRelativeTime(ts, now))
                            .createdAt(ts)
                            .unread(unread)
                            .targetUrl("/resume-analyzer")
                            .build());
                }
            }
        }

        // 4. Aptitude Assessment Attempts
        if (aptitudeAttemptRepository != null) {
            List<AptitudeAttempt> attempts = aptitudeAttemptRepository.findByUserOrderByCompletedAtDesc(user);
            for (AptitudeAttempt a : attempts) {
                LocalDateTime ts = a.getCompletedAt() != null ? a.getCompletedAt() : now;
                String id = "aptitude-" + a.getId();

                if ((dismissedAllBefore != null && !ts.isAfter(dismissedAllBefore)) || dismissedIds.contains(id)) {
                    continue;
                }

                boolean unread = lastRead == null || ts.isAfter(lastRead);
                String outcome = a.getPercentage() >= 60.0 ? "Passed" : "Completed";

                list.add(NotificationDto.builder()
                        .id(id)
                        .type("APTITUDE")
                        .title("Aptitude " + outcome + ": " + a.getTrackTitle())
                        .message("Scored " + Math.round(a.getPercentage()) + "% (" + a.getCorrectCount() + "/" + a.getTotalQuestions() + " correct) in " + formatSeconds(a.getTimeSpentSeconds()) + ".")
                        .timestamp(formatRelativeTime(ts, now))
                        .createdAt(ts)
                        .unread(unread)
                        .targetUrl("/aptitude")
                        .build());
            }
        }

        // 5. GitHub Analysis notifications
        if (githubAnalysisResultRepository != null) {
            List<GithubAnalysisResult> ghResults = githubAnalysisResultRepository.findByUserOrderByAnalyzedAtDesc(user);
            for (GithubAnalysisResult g : ghResults) {
                LocalDateTime ts = g.getAnalyzedAt() != null ? g.getAnalyzedAt() : now;
                String id = "github-" + g.getId();

                if ((dismissedAllBefore != null && !ts.isAfter(dismissedAllBefore)) || dismissedIds.contains(id)) {
                    continue;
                }

                boolean unread = lastRead == null || ts.isAfter(lastRead);
                list.add(NotificationDto.builder()
                        .id(id)
                        .type("GITHUB")
                        .title("GitHub Profile Score: " + g.getOverallScore() + "/100")
                        .message("Audit completed for @" + g.getGithubUsername() + ". Review actionable recruiter recommendations.")
                        .timestamp(formatRelativeTime(ts, now))
                        .createdAt(ts)
                        .unread(unread)
                        .targetUrl("/github-analyzer")
                        .build());
            }
        }

        // 6. AI Roadmap notifications
        if (userRoadmapRepository != null) {
            List<UserRoadmap> roadmaps = userRoadmapRepository.findByUserOrderByUpdatedAtDesc(user);
            for (UserRoadmap r : roadmaps) {
                LocalDateTime ts = r.getUpdatedAt() != null ? r.getUpdatedAt() : now;
                String id = "roadmap-" + r.getId();

                if ((dismissedAllBefore != null && !ts.isAfter(dismissedAllBefore)) || dismissedIds.contains(id)) {
                    continue;
                }

                boolean unread = lastRead == null || ts.isAfter(lastRead);
                list.add(NotificationDto.builder()
                        .id(id)
                        .type("ROADMAP")
                        .title("AI Roadmap Active: " + r.getTrackTitle())
                        .message("Progression tracked: " + r.getCompletedMilestonesCount() + " of " + r.getTotalMilestones() + " milestones (" + r.getXpEarned() + " XP).")
                        .timestamp(formatRelativeTime(ts, now))
                        .createdAt(ts)
                        .unread(unread)
                        .targetUrl("/ai-roadmap")
                        .build());
            }
        }

        // 7. Active Subscription notification
        if (subscriptionRepository != null) {
            List<Subscription> subs = subscriptionRepository.findByUserAndSubscriptionStatusOrderBySubscribedAtDesc(user, "ACTIVE");
            for (Subscription s : subs) {
                LocalDateTime ts = s.getSubscribedAt() != null ? s.getSubscribedAt() : now;
                String id = "sub-" + s.getId();

                if ((dismissedAllBefore != null && !ts.isAfter(dismissedAllBefore)) || dismissedIds.contains(id)) {
                    continue;
                }

                boolean unread = lastRead == null || ts.isAfter(lastRead);
                String planName = s.getPlan() != null ? s.getPlan().getName() : "Pro";
                list.add(NotificationDto.builder()
                        .id(id)
                        .type("SUBSCRIPTION")
                        .title("Active Membership: " + planName + " Plan")
                        .message("Your " + planName + " access is active with full platform features unlocked.")
                        .timestamp(formatRelativeTime(ts, now))
                        .createdAt(ts)
                        .unread(unread)
                        .targetUrl("/billing")
                        .build());
            }
        }

        // 8. Welcome notification if no events exist yet and user hasn't cleared notifications
        if (list.isEmpty() && !dismissedIds.contains("sys-welcome") && dismissedAllBefore == null) {
            boolean unread = lastRead == null;
            list.add(NotificationDto.builder()
                    .id("sys-welcome")
                    .type("SYSTEM")
                    .title("Welcome to Samprepix!")
                    .message("Your placement workspace is ready. Solve 6,260+ coding challenges, take aptitude assessments, or try an AI mock interview.")
                    .timestamp("Just now")
                    .createdAt(now)
                    .unread(unread)
                    .targetUrl("/coding-arena")
                    .build());
        }

        // Sort chronologically descending
        list.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        // Limit to 25 latest
        return list.stream().limit(25).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(User user) {
        return (int) getUserNotifications(user).stream()
                .filter(NotificationDto::isUnread)
                .count();
    }

    @Override
    public void markAllAsRead(User user) {
        user.setLastNotificationsReadAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void deleteNotification(User user, String notificationId) {
        if (notificationId == null || notificationId.trim().isEmpty()) {
            return;
        }
        if (user.getDismissedNotificationIds() == null) {
            user.setDismissedNotificationIds(new HashSet<>());
        }
        user.getDismissedNotificationIds().add(notificationId.trim());
        userRepository.save(user);
    }

    @Override
    public void clearAllNotifications(User user) {
        user.setDismissedAllNotificationsBefore(LocalDateTime.now());
        if (user.getDismissedNotificationIds() != null) {
            user.getDismissedNotificationIds().clear();
        }
        userRepository.save(user);
    }

    private LocalDateTime parseResumeTimestamp(String analyzedAt, LocalDateTime fallback) {
        if (analyzedAt == null || analyzedAt.isBlank()) {
            return fallback.minusDays(1);
        }
        try {
            return LocalDateTime.parse(analyzedAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(analyzedAt, DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
            } catch (Exception e2) {
                return fallback.minusDays(1);
            }
        }
    }

    private String formatSeconds(int totalSecs) {
        int mins = totalSecs / 60;
        int secs = totalSecs % 60;
        if (mins == 0) return secs + "s";
        return mins + "m " + secs + "s";
    }

    private String formatRelativeTime(LocalDateTime time, LocalDateTime now) {
        if (time == null) return "Recently";
        Duration d = Duration.between(time, now);
        long seconds = d.getSeconds();

        if (seconds < 60) {
            return "Just now";
        } else if (seconds < 3600) {
            long mins = seconds / 60;
            return mins + (mins == 1 ? " min ago" : " mins ago");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        } else if (seconds < 604800) {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        } else {
            return time.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
    }
}
