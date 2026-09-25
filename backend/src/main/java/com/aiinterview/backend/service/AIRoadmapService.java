package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.roadmap.RoadmapResponse;
import com.aiinterview.backend.dto.roadmap.RoadmapResponse.*;
import com.aiinterview.backend.entity.GithubAnalysisResult;
import com.aiinterview.backend.entity.ResumeAnalysis;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.entity.UserRoadmap;
import com.aiinterview.backend.repository.GithubAnalysisResultRepository;
import com.aiinterview.backend.repository.ResumeAnalysisRepository;
import com.aiinterview.backend.repository.UserProfileRepository;
import com.aiinterview.backend.repository.UserRoadmapRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIRoadmapService {

    private final UserRoadmapRepository userRoadmapRepository;
    private final UserProfileRepository userProfileRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final GithubAnalysisResultRepository githubAnalysisResultRepository;
    private final EntitlementService entitlementService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final List<TrackOptionDto> AVAILABLE_TRACKS = List.of(
            new TrackOptionDto("java-fullstack", "Java Full Stack Developer", "Spring Boot, React, MySQL, Microservices & JPA", "FaJava"),
            new TrackOptionDto("python-fullstack", "Python Full Stack Developer", "Django, FastAPI, React, PostgreSQL & Cloud", "FaPython"),
            new TrackOptionDto("mern", "MERN Stack Engineer", "MongoDB, Express, React, Node.js & REST APIs", "FaReact"),
            new TrackOptionDto("frontend", "Modern Frontend Engineer", "React, TypeScript, Next.js, Tailwind & Performance", "FiLayout"),
            new TrackOptionDto("backend", "Scalable Backend Engineer", "Distributed Systems, Caching, SQL/NoSQL & System Design", "FiServer"),
            new TrackOptionDto("ai-data", "AI & Data Science Engineer", "Python, Machine Learning, Deep Learning, SQL & Pandas", "FaBrain"),
            new TrackOptionDto("devops-cloud", "DevOps & Cloud Architect", "Docker, Kubernetes, AWS, CI/CD Pipelines & Terraform", "FaDocker"),
            new TrackOptionDto("mobile", "Cross-Platform Mobile Developer", "Flutter, Dart, Firebase, State Management & Mobile UX", "FaMobileAlt")
    );

    public static final List<String> INDUSTRY_SUGGESTIONS = List.of(
            "Flutter Developer",
            "Android Kotlin Developer",
            "iOS Swift Developer",
            "Cybersecurity Specialist",
            "Penetration Tester & Ethical Hacker",
            "Application Security Engineer (AppSec)",
            "Data Analyst & Business Intelligence",
            "Machine Learning Engineer",
            "Data Scientist",
            "Data Engineer (Spark & Airflow)",
            "Game Developer (Unity & C#)",
            "Unreal Engine C++ Developer",
            "Cloud & DevOps Architect",
            "Kubernetes Platform Engineer",
            "Site Reliability Engineer (SRE)",
            "Golang Microservices Engineer",
            "Rust Systems Developer",
            "Blockchain & Smart Contract Developer",
            "QA Automation & SDET",
            "Embedded Systems & IoT Engineer",
            "UI/UX Design Engineer"
    );

    public List<String> getTrackSuggestions(String query) {
        if (query == null || query.isBlank()) {
            return INDUSTRY_SUGGESTIONS;
        }
        String cleanQuery = query.trim().toLowerCase();
        List<String> matches = new ArrayList<>();
        for (String s : INDUSTRY_SUGGESTIONS) {
            if (s.toLowerCase().contains(cleanQuery)) {
                matches.add(s);
            }
        }
        boolean exactMatch = matches.stream().anyMatch(m -> m.equalsIgnoreCase(query.trim()));
        if (!exactMatch && query.trim().length() >= 2) {
            String customChoice = Character.toUpperCase(query.trim().charAt(0)) + query.trim().substring(1);
            matches.add(0, customChoice);
        }
        return matches;
    }

    @Transactional
    public RoadmapResponse getUserRoadmap(User user) {
        boolean isPremium = entitlementService.hasPremiumAccess(user);
        String effectivePlan = entitlementService.getEffectivePlan(user);

        // Find existing roadmap or detect and create one
        Optional<UserRoadmap> existingRoadmap = userRoadmapRepository.findFirstByUserOrderByUpdatedAtDesc(user);

        String detectedTrack = detectUserTrack(user);
        String trackId = existingRoadmap.map(UserRoadmap::getTrackId).orElse(detectedTrack);
        String customTitle = existingRoadmap.map(UserRoadmap::getTrackTitle).orElse(null);

        Set<String> completedIds = new HashSet<>();
        if (existingRoadmap.isPresent() && existingRoadmap.get().getCompletedMilestoneIdsJson() != null) {
            try {
                completedIds = objectMapper.readValue(existingRoadmap.get().getCompletedMilestoneIdsJson(), new TypeReference<>() {});
            } catch (Exception e) {
                log.warn("Failed to parse completed milestones for user {}", user.getId());
            }
        }

        return generateRoadmapResponse(trackId, customTitle, detectedTrack, completedIds, isPremium, effectivePlan);
    }

    public RoadmapResponse getDefaultRoadmap() {
        return generateRoadmapResponse("java-fullstack", null, "java-fullstack", Collections.emptySet(), false, "FREE");
    }

    public RoadmapResponse getPreviewForTrack(String trackId) {
        return getPreviewForTrack(trackId, null);
    }

    public RoadmapResponse getPreviewForTrack(String trackId, String customTitle) {
        if ("custom".equalsIgnoreCase(trackId)) {
            String title = (customTitle != null && !customTitle.isBlank()) ? customTitle.trim() : "Custom Specialized Track";
            return generateRoadmapResponse("custom", title, "custom", Collections.emptySet(), false, "FREE");
        }
        String safeTrackId = AVAILABLE_TRACKS.stream()
                .filter(t -> t.getId().equalsIgnoreCase(trackId))
                .map(TrackOptionDto::getId)
                .findFirst()
                .orElse("java-fullstack");
        return generateRoadmapResponse(safeTrackId, null, safeTrackId, Collections.emptySet(), false, "FREE");
    }

    @Transactional
    public RoadmapResponse switchTrack(User user, String newTrackId) {
        return switchTrack(user, newTrackId, null);
    }

    @Transactional
    public RoadmapResponse switchTrack(User user, String newTrackId, String customTitle) {
        boolean isPremium = entitlementService.hasPremiumAccess(user);
        String effectivePlan = entitlementService.getEffectivePlan(user);

        boolean isCustom = "custom".equalsIgnoreCase(newTrackId) ||
                AVAILABLE_TRACKS.stream().noneMatch(t -> t.getId().equalsIgnoreCase(newTrackId));

        String targetTrackId = isCustom ? "custom" : newTrackId;
        String trackTitle;
        if (isCustom) {
            if (customTitle != null && !customTitle.isBlank()) {
                trackTitle = customTitle.trim();
            } else if (!"custom".equalsIgnoreCase(newTrackId) && !newTrackId.isBlank()) {
                trackTitle = newTrackId.trim();
            } else {
                trackTitle = "Custom Career Track";
            }
        } else {
            trackTitle = getTrackTitle(targetTrackId);
        }

        String detectedTrack = detectUserTrack(user);
        Optional<UserRoadmap> existingForTrack = userRoadmapRepository.findByUserAndTrackId(user, targetTrackId);

        Set<String> completedIds = new HashSet<>();
        if (existingForTrack.isPresent() && existingForTrack.get().getCompletedMilestoneIdsJson() != null) {
            try {
                completedIds = objectMapper.readValue(existingForTrack.get().getCompletedMilestoneIdsJson(), new TypeReference<>() {});
            } catch (Exception e) {
                log.warn("Failed to parse completed milestones for track {}", targetTrackId);
            }
        }

        // Save switch
        UserRoadmap roadmap = existingForTrack.orElseGet(() -> UserRoadmap.builder()
                .user(user)
                .trackId(targetTrackId)
                .trackTitle(trackTitle)
                .detectedTrack(detectedTrack)
                .completedMilestoneIdsJson("[]")
                .completedMilestonesCount(0)
                .xpEarned(0)
                .build());

        roadmap.setTrackTitle(trackTitle);
        userRoadmapRepository.save(roadmap);

        return generateRoadmapResponse(targetTrackId, trackTitle, detectedTrack, completedIds, isPremium, effectivePlan);
    }

    @Transactional
    public RoadmapResponse toggleMilestone(User user, String trackId, String milestoneId) {
        boolean isPremium = entitlementService.hasPremiumAccess(user);
        String effectivePlan = entitlementService.getEffectivePlan(user);

        UserRoadmap roadmap = userRoadmapRepository.findByUserAndTrackId(user, trackId)
                .orElseGet(() -> UserRoadmap.builder()
                        .user(user)
                        .trackId(trackId)
                        .trackTitle(getTrackTitle(trackId))
                        .detectedTrack(detectUserTrack(user))
                        .completedMilestoneIdsJson("[]")
                        .build());

        Set<String> completedIds = new HashSet<>();
        if (roadmap.getCompletedMilestoneIdsJson() != null && !roadmap.getCompletedMilestoneIdsJson().isBlank()) {
            try {
                completedIds = objectMapper.readValue(roadmap.getCompletedMilestoneIdsJson(), new TypeReference<>() {});
            } catch (Exception e) {
                completedIds = new HashSet<>();
            }
        }

        if (completedIds.contains(milestoneId)) {
            completedIds.remove(milestoneId);
        } else {
            completedIds.add(milestoneId);
        }

        try {
            roadmap.setCompletedMilestoneIdsJson(objectMapper.writeValueAsString(completedIds));
            roadmap.setCompletedMilestonesCount(completedIds.size());
            userRoadmapRepository.save(roadmap);
        } catch (Exception e) {
            log.error("Failed to update milestone status: {}", e.getMessage());
        }

        return generateRoadmapResponse(trackId, roadmap.getTrackTitle(), roadmap.getDetectedTrack(), completedIds, isPremium, effectivePlan);
    }

    private String detectUserTrack(User user) {
        if (user == null) return "java-fullstack";

        // Priority 1: GitHub Repositories Analysis
        Optional<GithubAnalysisResult> ghOpt = githubAnalysisResultRepository.findFirstByUserOrderByAnalyzedAtDesc(user);
        if (ghOpt.isPresent() && ghOpt.get().getTopLanguagesJson() != null) {
            String lowerLangs = ghOpt.get().getTopLanguagesJson().toLowerCase();
            if (lowerLangs.contains("python")) {
                return "python-fullstack";
            }
            if (lowerLangs.contains("java") && !lowerLangs.contains("javascript")) {
                return "java-fullstack";
            }
            if (lowerLangs.contains("javascript") || lowerLangs.contains("typescript")) {
                return "frontend";
            }
            if (lowerLangs.contains("dart") || lowerLangs.contains("flutter")) {
                return "mobile";
            }
            if (lowerLangs.contains("docker") || lowerLangs.contains("terraform") || lowerLangs.contains("hcl")) {
                return "devops-cloud";
            }
        }

        // Priority 2: UserProfile targetRole & skills
        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
        if (profile != null) {
            String target = profile.getTargetRole() != null ? profile.getTargetRole().toLowerCase() : "";
            List<String> skills = profile.getSkills() != null ? profile.getSkills() : Collections.emptyList();
            String skillsStr = String.join(" ", skills).toLowerCase();

            if (target.contains("java") || skillsStr.contains("java") || skillsStr.contains("spring")) {
                return "java-fullstack";
            }
            if (target.contains("ai") || target.contains("data") || target.contains("machine learning") || skillsStr.contains("pandas") || skillsStr.contains("tensorflow") || skillsStr.contains("pytorch")) {
                return "ai-data";
            }
            if (target.contains("python") || skillsStr.contains("fastapi") || skillsStr.contains("django")) {
                return "python-fullstack";
            }
            if (target.contains("devops") || target.contains("cloud") || target.contains("sre") || skillsStr.contains("docker") || skillsStr.contains("kubernetes") || skillsStr.contains("aws")) {
                return "devops-cloud";
            }
            if (target.contains("frontend") || target.contains("react") || target.contains("ui") || target.contains("web") || skillsStr.contains("css") || skillsStr.contains("tailwind")) {
                return "frontend";
            }
            if (target.contains("backend") || target.contains("microservices") || target.contains("api") || skillsStr.contains("system design")) {
                return "backend";
            }
            if (target.contains("mobile") || target.contains("flutter") || target.contains("android") || target.contains("ios") || skillsStr.contains("flutter") || skillsStr.contains("react native")) {
                return "mobile";
            }
            if (skillsStr.contains("node") || skillsStr.contains("mongo") || skillsStr.contains("express")) {
                return "mern";
            }
        }

        // Priority 3: Resume Analysis
        List<ResumeAnalysis> resumes = resumeAnalysisRepository.findByUserEmail(user.getEmail());
        if (resumes != null && !resumes.isEmpty()) {
            String resumeSkills = resumes.get(0).getSkills() != null ? resumes.get(0).getSkills().toLowerCase() : "";
            if (resumeSkills.contains("python") && (resumeSkills.contains("ml") || resumeSkills.contains("data"))) return "ai-data";
            if (resumeSkills.contains("python")) return "python-fullstack";
            if (resumeSkills.contains("java") || resumeSkills.contains("spring")) return "java-fullstack";
            if (resumeSkills.contains("docker") || resumeSkills.contains("kubernetes") || resumeSkills.contains("aws")) return "devops-cloud";
            if (resumeSkills.contains("react") || resumeSkills.contains("html") || resumeSkills.contains("frontend")) return "frontend";
            if (resumeSkills.contains("mern") || resumeSkills.contains("node") || resumeSkills.contains("mongo")) return "mern";
            if (resumeSkills.contains("flutter") || resumeSkills.contains("android") || resumeSkills.contains("ios")) return "mobile";
        }

        return "java-fullstack";
    }

    private String getTrackTitle(String trackId) {
        return AVAILABLE_TRACKS.stream()
                .filter(t -> t.getId().equalsIgnoreCase(trackId))
                .map(TrackOptionDto::getTitle)
                .findFirst()
                .orElse("Software Engineering Career Roadmap");
    }

    private RoadmapResponse generateRoadmapResponse(
            String trackId, String customTitle, String detectedTrackId, Set<String> completedIds, boolean isPremium, String effectivePlan
    ) {
        String resolvedTitle = (customTitle != null && !customTitle.isBlank()) ? customTitle : getTrackTitle(trackId);
        List<RoadmapPhaseDto> phases = buildPhasesForTrack(trackId, resolvedTitle, completedIds, isPremium);

        int totalMilestones = 0;
        int completedCount = 0;
        int totalXp = 0;
        int earnedXp = 0;

        for (RoadmapPhaseDto phase : phases) {
            for (MilestoneDto m : phase.getMilestones()) {
                totalMilestones++;
                totalXp += m.getXp();
                if (m.isCompleted()) {
                    completedCount++;
                    earnedXp += m.getXp();
                }
            }
        }

        int progressPct = totalMilestones > 0 ? (int) Math.round(((double) completedCount / totalMilestones) * 100) : 0;
        String level = calculateLevel(earnedXp);

        return RoadmapResponse.builder()
                .trackId(trackId)
                .trackTitle(resolvedTitle)
                .detectedTrack(detectedTrackId)
                .description("AI-generated specialized placement roadmap aligning your resume, github, and role expectations.")
                .totalMilestones(totalMilestones)
                .completedMilestonesCount(completedCount)
                .progressPercentage(progressPct)
                .totalXp(totalXp)
                .earnedXp(earnedXp)
                .currentLevel(level)
                .phases(phases)
                .availableTracks(AVAILABLE_TRACKS)
                .premium(isPremium)
                .effectivePlan(effectivePlan)
                .build();
    }

    private String calculateLevel(int xp) {
        if (xp >= 1500) return "Level 5: Placement Master (Ready for Tier-1 FAANG/MNC)";
        if (xp >= 1000) return "Level 4: Advanced Engineer";
        if (xp >= 600) return "Level 3: Full Stack Builder";
        if (xp >= 300) return "Level 2: Core Developer";
        return "Level 1: Foundation Explorer";
    }

    private List<RoadmapPhaseDto> buildPhasesForTrack(String trackId, String customTitle, Set<String> completedIds, boolean isPremium) {
        List<RoadmapPhaseDto> phases = new ArrayList<>();

        if ("custom".equalsIgnoreCase(trackId)) {
            return buildPhasesForCustomTrack(customTitle, completedIds, isPremium);
        }

        if ("python-fullstack".equalsIgnoreCase(trackId)) {
            phases.add(createPhase("py-p1", 1, "Foundation & Tooling", "Git, Environment Setup & Shell Proficiency", false, List.of(
                    createMilestone("py-m1", "Git & GitHub Mastery", "Branching, PRs, merge conflict resolution and interactive rebasing.", "4 hours", 50, List.of("Git", "GitHub", "Terminal"), "Host an open-source CLI toolkit on GitHub", completedIds),
                    createMilestone("py-m2", "Python Advanced Syntax", "Decorators, generators, context managers, and type annotations.", "8 hours", 75, List.of("Python 3.12", "Type Hints", "Decorators"), "Custom logging and retry decorator utility", completedIds)
            )));
            phases.add(createPhase("py-p2", 2, "Object-Oriented Design & DSA", "Data Structures, Algorithms & Design Patterns", false, List.of(
                    createMilestone("py-m3", "OOP Architecture in Python", "Polymorphism, dunder methods, metaclasses and solid principles.", "6 hours", 75, List.of("OOP", "Clean Code", "Design Patterns"), "Bank management system with transaction audit logs", completedIds),
                    createMilestone("py-m4", "Core DSA & Problem Solving", "Arrays, HashMaps, Trees, Graphs, Two-Pointers, and Dynamic Programming.", "15 hours", 100, List.of("DSA", "Time Complexity", "Algorithms"), "Solve 50+ medium LeetCode/Samprepix challenges", completedIds)
            )));
            phases.add(createPhase("py-p3", 3, "Database Engineering & SQL", "Relational Modeling, Query Optimization & Indexing", !isPremium, List.of(
                    createMilestone("py-m5", "PostgreSQL & Complex SQL", "Joins, Window functions, CTEs, Transactions and ACID properties.", "8 hours", 80, List.of("PostgreSQL", "SQL", "Database Design"), "E-commerce relational schema with analytical queries", completedIds, !isPremium),
                    createMilestone("py-m6", "SQLAlchemy ORM & Migrations", "Alembic migrations, one-to-many, many-to-many, and eager loading.", "6 hours", 75, List.of("SQLAlchemy", "Alembic", "ORM"), "Multi-tenant inventory database backend", completedIds, !isPremium)
            )));
            phases.add(createPhase("py-p4", 4, "Backend APIs with FastAPI & Django", "High-Performance RESTful Microservices", !isPremium, List.of(
                    createMilestone("py-m7", "FastAPI Asynchronous Endpoints", "Async/await, Pydantic schemas, dependency injection and Swagger docs.", "10 hours", 90, List.of("FastAPI", "Pydantic", "AsyncIO"), "Real-time task management REST API", completedIds, !isPremium),
                    createMilestone("py-m8", "Background Workers & Redis Caching", "Celery async worker queues, Redis cache invalidation and rate limiting.", "8 hours", 85, List.of("Redis", "Celery", "Distributed Caching"), "Asynchronous report generation service", completedIds, !isPremium)
            )));
            phases.add(createPhase("py-p5", 5, "Frontend Integration & React", "Single Page Applications & State Management", !isPremium, List.of(
                    createMilestone("py-m9", "React 19 & Modern Hooks", "Component lifecycle, custom hooks, Tailwind CSS, and Axios client.", "10 hours", 80, List.of("React", "Tailwind CSS", "Axios"), "Modern analytics dashboard UI", completedIds, !isPremium)
            )));
            phases.add(createPhase("py-p6", 6, "Security, JWT & OAuth2", "Authentication, Authorization & Role-Based Access", !isPremium, List.of(
                    createMilestone("py-m10", "JWT & OAuth2 Social Login", "Access/refresh token lifecycles, password hashing (Argon2), and RBAC.", "8 hours", 90, List.of("JWT", "OAuth2", "Security"), "Unified identity auth provider microservice", completedIds, !isPremium)
            )));
            phases.add(createPhase("py-p7", 7, "Flagship Production Project", "End-to-End Enterprise Solution", !isPremium, List.of(
                    createMilestone("py-m11", "AI-Powered Automation Platform", "Full stack application featuring background workers, vector search, and webhooks.", "20 hours", 150, List.of("Full Stack", "AI Integration", "FastAPI", "React"), "AI Placement Simulation Suite", completedIds, !isPremium)
            )));
            phases.add(createPhase("py-p8", 8, "Mock Interviews & Placement Prep", "System Design & Technical Screen Readiness", !isPremium, List.of(
                    createMilestone("py-m12", "System Design & Behavioral Readiness", "Scalability, load balancers, database sharding, and STAR technique answers.", "12 hours", 100, List.of("System Design", "Scalability", "Interview"), "Complete 5 mock interviews on Samprepix", completedIds, !isPremium)
            )));
            return phases;
        }

        if ("mern".equalsIgnoreCase(trackId)) {
            phases.add(createPhase("mern-p1", 1, "Modern JavaScript & TypeScript", "ES2024+, Node.js Runtime & Typing", false, List.of(
                    createMilestone("mern-m1", "Advanced JavaScript & Event Loop", "Closures, Event Loop, Promises, Microtasks, and Prototypes.", "6 hours", 50, List.of("JavaScript", "Node.js", "Async"), "Custom Promise pool and rate-limiter utility", completedIds),
                    createMilestone("mern-m2", "TypeScript Strict Typing", "Generics, Utility Types, Type Narrowing, and Interfaces.", "8 hours", 75, List.of("TypeScript", "Strict Mode", "Generics"), "Typed REST response validator library", completedIds)
            )));
            phases.add(createPhase("mern-p2", 2, "DSA in JavaScript / TypeScript", "Core Problem Solving & Complexity Analysis", false, List.of(
                    createMilestone("mern-m3", "Data Structures with TS", "Arrays, HashMaps, Linked Lists, Trees, and Graph traversals (BFS/DFS).", "12 hours", 80, List.of("DSA", "TypeScript", "Graph"), "Graph-based dependency resolution engine", completedIds),
                    createMilestone("mern-m4", "Coding Arena MERN Sprint", "Solve 50+ DSA problems in the Samprepix Arena using JS/TS.", "15 hours", 100, List.of("Algorithms", "DP", "Time Complexity"), "Achieve 5-day streak in Coding Arena", completedIds)
            )));
            phases.add(createPhase("mern-p3", 3, "MongoDB & Document Modeling", "Aggregation Pipelines & Schema Design", !isPremium, List.of(
                    createMilestone("mern-m5", "MongoDB Aggregation & Indexing", "Compound indexes, Atlas Search, Lookups, and Sharding concepts.", "8 hours", 80, List.of("MongoDB", "Aggregation", "Indexes"), "E-commerce analytical reporting aggregation pipeline", completedIds, !isPremium),
                    createMilestone("mern-m6", "Mongoose ODM & Validation", "Schema plugins, hooks, discriminators, and population optimization.", "6 hours", 75, List.of("Mongoose", "Validation", "NoSQL"), "Multi-vendor store data modeling with soft deletes", completedIds, !isPremium)
            )));
            phases.add(createPhase("mern-p4", 4, "Express.js & RESTful Architecture", "Production APIs, Error Handling & Auth", !isPremium, List.of(
                    createMilestone("mern-m7", "Express 5 Production Architecture", "Controller-service pattern, custom middlewares, and central error handling.", "10 hours", 90, List.of("Express.js", "REST", "Clean Architecture"), "Enterprise content management REST API", completedIds, !isPremium),
                    createMilestone("mern-m8", "JWT Authentication & RBAC", "HTTP-only cookie refresh rotation, CSRF protection, and role permissions.", "8 hours", 90, List.of("JWT", "Security", "RBAC"), "Role-based auth service with session revocation", completedIds, !isPremium)
            )));
            phases.add(createPhase("mern-p5", 5, "React 19 & State Architecture", "Modern Hooks, Tailwind & State Management", !isPremium, List.of(
                    createMilestone("mern-m9", "React 19 Components & Hooks", "Server components, Actions, Zustand state store, and responsive UI.", "12 hours", 85, List.of("React 19", "Zustand", "Tailwind CSS"), "Collaborative Kanban board with optimistic updates", completedIds, !isPremium)
            )));
            phases.add(createPhase("mern-p6", 6, "Real-time Sockets & Redis Caching", "WebSockets, Message Queues & Performance", !isPremium, List.of(
                    createMilestone("mern-m10", "Socket.IO & BullMQ / Redis", "Bidirectional event streams, BullMQ background job queues, and caching.", "10 hours", 95, List.of("Socket.IO", "Redis", "BullMQ"), "Live collaborative whiteboard and notifications service", completedIds, !isPremium)
            )));
            phases.add(createPhase("mern-p7", 7, "Flagship MERN Platform", "Full Stack Enterprise Application", !isPremium, List.of(
                    createMilestone("mern-m11", "Production SaaS with Payment & Cloud", "Stripe/Cashfree billing, AWS S3 file pipeline, and webhook processing.", "20 hours", 150, List.of("Full Stack", "MERN", "AWS", "Payments"), "Full-stack Developer Community & Mentorship Platform", completedIds, !isPremium)
            )));
            phases.add(createPhase("mern-p8", 8, "System Design & Placement Interviews", "Architecture, Scale & Technical Screen", !isPremium, List.of(
                    createMilestone("mern-m12", "Node.js Concurrency & System Design", "Cluster module, PM2, horizontal scaling, and AI Mock Interviews.", "12 hours", 100, List.of("System Design", "Node.js Scale", "Mock Interviews"), "Complete 5 mock interviews on Samprepix", completedIds, !isPremium)
            )));
            return phases;
        }

        if ("frontend".equalsIgnoreCase(trackId)) {
            phases.add(createPhase("fe-p1", 1, "Modern Web Standards & CSS", "Semantic HTML5, Responsive Layouts & A11y", false, List.of(
                    createMilestone("fe-m1", "Semantic HTML & A11y Standards", "ARIA landmarks, keyboard navigation, contrast ratio compliance, and screen readers.", "6 hours", 50, List.of("HTML5", "a11y", "ARIA"), "Fully accessible government portal UI template", completedIds),
                    createMilestone("fe-m2", "Modern CSS & Responsive Systems", "Flexbox, Grid, Container Queries, CSS Variables, and Tailwind CSS.", "8 hours", 75, List.of("CSS Grid", "Tailwind CSS", "Container Queries"), "Fluid design system component library", completedIds)
            )));
            phases.add(createPhase("fe-p2", 2, "JavaScript Deep Dive & TypeScript", "Event Loop, Closures, Async & Types", false, List.of(
                    createMilestone("fe-m3", "Vanilla JS Deep Dive", "Prototypes, event bubbling, debouncing, throttling, and memory leak debugging.", "10 hours", 80, List.of("JavaScript", "DOM", "Performance"), "Interactive zero-dependency drag-and-drop tree view", completedIds),
                    createMilestone("fe-m4", "TypeScript for Modern Frontends", "Generics, discrimination unions, strict types, and React types.", "10 hours", 85, List.of("TypeScript", "Generics", "React TS"), "Type-safe form builder engine", completedIds)
            )));
            phases.add(createPhase("fe-p3", 3, "React 19 & Component Architecture", "Custom Hooks, Suspense & Performance", !isPremium, List.of(
                    createMilestone("fe-m5", "React 19 Core & Custom Hooks", "useId, useTransition, useDeferredValue, and reusable business logic hooks.", "10 hours", 85, List.of("React 19", "Hooks", "Suspense"), "Infinite scroll virtualized data table", completedIds, !isPremium),
                    createMilestone("fe-m6", "State Management (Zustand & TanStack Query)", "Server state sync, caching, optimistic updates, and offline sync.", "8 hours", 85, List.of("TanStack Query", "Zustand", "State Sync"), "Real-time stock ticker with query cache", completedIds, !isPremium)
            )));
            phases.add(createPhase("fe-p4", 4, "Next.js App Router & SSR", "Server Components, SEO & Edge Routing", !isPremium, List.of(
                    createMilestone("fe-m7", "Next.js 15 App Router & Server Actions", "RSC, layout nesting, parallel routes, and mutations without API routes.", "12 hours", 95, List.of("Next.js", "RSC", "Server Actions"), "SEO-optimized e-commerce storefront", completedIds, !isPremium)
            )));
            phases.add(createPhase("fe-p5", 5, "Design Systems & Animation", "Micro-interactions & Component Design", !isPremium, List.of(
                    createMilestone("fe-m8", "Framer Motion & Micro-Interactions", "Spring physics, layout animations, gestures, and layout morphing.", "8 hours", 80, List.of("Framer Motion", "Animations", "UI/UX"), "Animated executive analytics dashboard", completedIds, !isPremium)
            )));
            phases.add(createPhase("fe-p6", 6, "Performance & Testing", "Core Web Vitals, Vitest & Playwright", !isPremium, List.of(
                    createMilestone("fe-m9", "Core Web Vitals Optimization", "LCP, INP, CLS debugging, bundle analyzer, dynamic imports, and lazy loading.", "8 hours", 90, List.of("CWV", "LCP", "INP", "Bundle Size"), "Achieve 98+ Lighthouse score on complex dashboard", completedIds, !isPremium),
                    createMilestone("fe-m10", "Automated Testing with Vitest & Playwright", "Unit tests with Testing Library, mocking APIs, and E2E regression tests.", "10 hours", 85, List.of("Vitest", "Playwright", "Testing Library"), "Complete automated test suite with CI verification", completedIds, !isPremium)
            )));
            phases.add(createPhase("fe-p7", 7, "Flagship Production Web App", "Full-Featured Enterprise SaaS Frontend", !isPremium, List.of(
                    createMilestone("fe-m11", "Enterprise Cloud Workflow Builder", "Canvas interactions, undo/redo state, keyboard shortcuts, and dark mode.", "20 hours", 150, List.of("React", "Next.js", "Design System", "TypeScript"), "No-Code Workflow Automation Visual Canvas", completedIds, !isPremium)
            )));
            phases.add(createPhase("fe-p8", 8, "Machine Coding & Placement Interviews", "Frontend System Design & Speed Coding", !isPremium, List.of(
                    createMilestone("fe-m12", "Frontend System Design & Mock Screens", "Micro-frontends, caching strategies, and live machine coding challenges.", "12 hours", 100, List.of("System Design", "Machine Coding", "Interview"), "Pass 5 frontend mock interviews on Samprepix", completedIds, !isPremium)
            )));
            return phases;
        }

        if ("backend".equalsIgnoreCase(trackId)) {
            phases.add(createPhase("be-p1", 1, "Operating Systems & Networking", "Concurrency, Memory, Protocols & Linux", false, List.of(
                    createMilestone("be-m1", "Processes, Threads & Concurrency", "Multi-threading, mutexes, deadlocks, CPU cache lines, and context switching.", "8 hours", 60, List.of("OS", "Concurrency", "Linux"), "High-concurrency thread pool worker in Java/Go", completedIds),
                    createMilestone("be-m2", "Network Protocols & Socket Programming", "TCP/UDP handshake, HTTP/1.1 vs HTTP/2 vs HTTP/3, TLS handshake, and WebSockets.", "8 hours", 65, List.of("TCP/IP", "HTTP/2", "TLS", "Networking"), "Raw TCP custom protocol client and server", completedIds)
            )));
            phases.add(createPhase("be-p2", 2, "Advanced DSA & Performance Optimization", "Algorithmic Complexity & Big-O", false, List.of(
                    createMilestone("be-m3", "Advanced Data Structures", "Trie, Segment Trees, Disjoint Set Union, LRU Cache, and Bloom Filters.", "12 hours", 85, List.of("DSA", "Bloom Filter", "LRU Cache"), "Custom in-memory LRU cache with O(1) operations", completedIds),
                    createMilestone("be-m4", "Coding Arena Backend Sprint", "Solve 50+ Hard algorithmic problems in the Samprepix Arena.", "15 hours", 100, List.of("Algorithms", "DP", "Graph Theory"), "Achieve 5-day streak in Coding Arena", completedIds)
            )));
            phases.add(createPhase("be-p3", 3, "Database Architecture & SQL Optimization", "Relational Modeling, Indexing & NoSQL", !isPremium, List.of(
                    createMilestone("be-m5", "PostgreSQL / MySQL Internal Mechanics", "B-Tree vs Hash indexes, EXPLAIN ANALYZE, WAL logs, and transaction isolation levels.", "10 hours", 90, List.of("PostgreSQL", "SQL Tuning", "B-Trees"), "Query optimizer benchmark analyzing 1M+ rows", completedIds, !isPremium),
                    createMilestone("be-m6", "Distributed Caching with Redis", "Cache-aside, write-through, cache stampede mitigation, and TTL invalidation.", "8 hours", 85, List.of("Redis", "Caching", "Distributed Systems"), "Distributed session and rate limiter using Redis Lua", completedIds, !isPremium)
            )));
            phases.add(createPhase("be-p4", 4, "High-Performance APIs (gRPC & REST)", "Clean Architecture & Protocol Buffers", !isPremium, List.of(
                    createMilestone("be-m7", "RESTful Best Practices & Idempotency", "Idempotency keys, versioning, pagination, rate limiting, and RFC 7807 error responses.", "10 hours", 90, List.of("REST", "API Design", "Idempotency"), "Production financial ledger API with idempotency", completedIds, !isPremium),
                    createMilestone("be-m8", "gRPC & Protocol Buffers Microservices", "Proto3 schemas, bidirectional streaming, and low-latency inter-service calls.", "8 hours", 95, List.of("gRPC", "Protobuf", "Microservices"), "Internal analytics streaming microservice via gRPC", completedIds, !isPremium)
            )));
            phases.add(createPhase("be-p5", 5, "Distributed Messaging with Kafka", "Event-Driven Architecture & Outbox Pattern", !isPremium, List.of(
                    createMilestone("be-m9", "Apache Kafka & Event Streaming", "Partitions, consumer groups, offset management, exactly-once semantics, and transactional outbox.", "12 hours", 100, List.of("Apache Kafka", "Event-Driven", "Outbox Pattern"), "Order fulfillment event pipeline with Kafka", completedIds, !isPremium)
            )));
            phases.add(createPhase("be-p6", 6, "Security, OAuth2 & Resiliency", "Stateless Tokens, Circuit Breakers & Governance", !isPremium, List.of(
                    createMilestone("be-m10", "OAuth2.0 / OpenID Connect & RBAC", "PKCE flow, asymmetric key verification (RS256), and fine-grained permissions.", "8 hours", 90, List.of("OAuth2", "Security", "JWT"), "Centralized authentication and identity provider service", completedIds, !isPremium),
                    createMilestone("be-m11", "Resiliency Patterns (Resilience4j / Istio)", "Circuit breakers, bulkheads, retries, and exponential backoff strategies.", "8 hours", 85, List.of("Circuit Breaker", "Resilience", "Fault Tolerance"), "Fault-tolerant payment gateway integration proxy", completedIds, !isPremium)
            )));
            phases.add(createPhase("be-p7", 7, "Flagship Distributed Backend Platform", "High-Throughput Enterprise Backend", !isPremium, List.of(
                    createMilestone("be-m12", "Scalable URL Shortener / Analytics Engine", "Handling 50K req/sec, consistent hashing, database sharding, and write-heavy workloads.", "20 hours", 150, List.of("Distributed Systems", "Sharding", "Kafka", "Redis"), "Global Distributed URL Analytics & Redirection Engine", completedIds, !isPremium)
            )));
            phases.add(createPhase("be-p8", 8, "System Design & Placement Screenings", "High-Level & Low-Level Design (HLD/LLD)", !isPremium, List.of(
                    createMilestone("be-m13", "HLD/LLD Architecture & Mock Interviews", "Designing WhatsApp, Uber, Netflix, and rate limiters with real-time AI feedback.", "15 hours", 110, List.of("System Design", "HLD", "LLD", "Interview Prep"), "Complete 5 system design mock interviews on Samprepix", completedIds, !isPremium)
            )));
            return phases;
        }

        if ("ai-data".equalsIgnoreCase(trackId)) {
            phases.add(createPhase("ai-p1", 1, "Python for Data Science & Math", "NumPy, Vectorization & Linear Algebra", false, List.of(
                    createMilestone("ai-m1", "NumPy & Vectorized Math", "Broadcasting, matrix multiplications, dot products, and vectorized algorithms.", "8 hours", 60, List.of("Python", "NumPy", "Linear Algebra"), "Neural network forward pass from scratch in NumPy", completedIds),
                    createMilestone("ai-m2", "Probability, Statistics & Calculus", "Distributions, hypothesis testing, Bayes theorem, gradients, and cost functions.", "8 hours", 65, List.of("Statistics", "Probability", "Calculus"), "A/B testing simulation framework", completedIds)
            )));
            phases.add(createPhase("ai-p2", 2, "Data Wrangling & SQL for Data Science", "Pandas, Analytical SQL & Feature Prep", false, List.of(
                    createMilestone("ai-m3", "Advanced Pandas & Data Cleaning", "Multi-indexing, groupby aggregations, window operations, and missing value imputation.", "10 hours", 80, List.of("Pandas", "EDA", "Data Cleaning"), "Financial market trend analysis pipeline", completedIds),
                    createMilestone("ai-m4", "Analytical SQL & BigQuery / DuckDB", "CTEs, window functions, percentile ranking, and high-speed analytical queries.", "8 hours", 80, List.of("SQL", "DuckDB", "Analytics"), "Customer lifetime value analytics dashboard dataset", completedIds)
            )));
            phases.add(createPhase("ai-p3", 3, "Classical Machine Learning", "Scikit-Learn, Regression & Classification", !isPremium, List.of(
                    createMilestone("ai-m5", "Supervised Learning Models", "Linear/Logistic Regression, Random Forests, Gradient Boosting (XGBoost, LightGBM).", "12 hours", 90, List.of("Scikit-Learn", "XGBoost", "Machine Learning"), "Predictive loan default classification model", completedIds, !isPremium),
                    createMilestone("ai-m6", "Model Evaluation & Cross-Validation", "ROC-AUC, Precision-Recall, Confusion Matrix, Hyperparameter tuning (Optuna).", "8 hours", 85, List.of("Model Tuning", "Optuna", "Validation"), "Automated ML hyperparameter optimization pipeline", completedIds, !isPremium)
            )));
            phases.add(createPhase("ai-p4", 4, "Deep Learning Foundations (PyTorch)", "Neural Networks, Backprop & Optimization", !isPremium, List.of(
                    createMilestone("ai-m7", "PyTorch Core & Tensors", "Autograd, custom nn.Module, loss functions, SGD, AdamW, and learning rate schedulers.", "12 hours", 95, List.of("PyTorch", "Deep Learning", "Tensors"), "Custom image classifier trained on CIFAR-10", completedIds, !isPremium),
                    createMilestone("ai-m8", "CNNs & Computer Vision Basics", "Convolutional layers, pooling, transfer learning with ResNet, and data augmentation.", "10 hours", 90, List.of("Computer Vision", "CNN", "Transfer Learning"), "Medical chest X-ray disease detection model", completedIds, !isPremium)
            )));
            phases.add(createPhase("ai-p5", 5, "Transformers & NLP Fundamentals", "Self-Attention, BERT, Hugging Face & Tokenization", !isPremium, List.of(
                    createMilestone("ai-m9", "Self-Attention & Transformer Architecture", "Multi-head attention, positional encoding, and Hugging Face Transformers.", "12 hours", 100, List.of("Transformers", "NLP", "Hugging Face"), "Customer sentiment & intent analysis NLP model", completedIds, !isPremium)
            )));
            phases.add(createPhase("ai-p6", 6, "Generative AI, LLMs & RAG", "Vector Databases, Embeddings & LangChain", !isPremium, List.of(
                    createMilestone("ai-m10", "Retrieval Augmented Generation (RAG)", "Embeddings, vector databases (Pinecone, ChromaDB, PGVector), and chunking strategies.", "12 hours", 100, List.of("RAG", "LLMs", "Vector DB", "Embeddings"), "Domain-specific AI document question-answering assistant", completedIds, !isPremium)
            )));
            phases.add(createPhase("ai-p7", 7, "Flagship Production AI Platform", "End-to-End MLOps & API Deployment", !isPremium, List.of(
                    createMilestone("ai-m11", "Production AI System with FastAPI & Docker", "Model serving, latency optimization, Dockerization, and asynchronous batch inference.", "20 hours", 150, List.of("MLOps", "FastAPI", "Docker", "Model Serving"), "AI Automated Resume & Code Screening Platform", completedIds, !isPremium)
            )));
            phases.add(createPhase("ai-p8", 8, "ML System Design & Placement Prep", "Scale, Evaluation & Technical Interviews", !isPremium, List.of(
                    createMilestone("ai-m12", "ML System Design & Technical Screen", "Designing recommendation systems (YouTube/TikTok), fraud detection, and AI Mock Interviews.", "12 hours", 100, List.of("ML System Design", "Scalability", "Interview"), "Complete 5 AI/ML mock interviews on Samprepix", completedIds, !isPremium)
            )));
            return phases;
        }

        if ("devops-cloud".equalsIgnoreCase(trackId)) {
            phases.add(createPhase("do-p1", 1, "Linux Administration & Shell Scripting", "System Internals, Bash Automation & Networking", false, List.of(
                    createMilestone("do-m1", "Linux Administration & Permissions", "File permissions, systemd services, process signals, SSH keys, and firewall (UFW).", "8 hours", 60, List.of("Linux", "Bash", "Systemd"), "Automated server hardening and setup script", completedIds),
                    createMilestone("do-m2", "Networking & DNS for DevOps", "DNS resolution, reverse proxies (Nginx), SSL/TLS certificates (Let's Encrypt), and subnetting.", "8 hours", 65, List.of("Nginx", "DNS", "SSL/TLS", "Networking"), "Load-balanced Nginx reverse proxy with SSL auto-renewal", completedIds)
            )));
            phases.add(createPhase("do-p2", 2, "Git & CI/CD Automation", "GitHub Actions, Workflows & Pipeline Security", false, List.of(
                    createMilestone("do-m3", "GitHub Actions & Pipeline Engineering", "Matrix builds, secrets management, linting, unit test gates, and Docker image pushing.", "10 hours", 80, List.of("CI/CD", "GitHub Actions", "Automation"), "Production CI pipeline with security scanning (Trivy)", completedIds),
                    createMilestone("do-m4", "Coding Arena Cloud Automation Sprint", "Solve 50+ automation and problem solving challenges in Samprepix Arena.", "15 hours", 100, List.of("Automation", "Algorithms", "Scripts"), "Achieve 5-day streak in Coding Arena", completedIds)
            )));
            phases.add(createPhase("do-p3", 3, "Containerization with Docker", "Multi-Stage Builds, Compose & Security", !isPremium, List.of(
                    createMilestone("do-m5", "Docker Deep Dive & Multi-Stage Builds", "Minimizing image size, non-root users, build cache, and Docker Compose networks.", "10 hours", 85, List.of("Docker", "Containerization", "Security"), "Multi-service containerized microservices stack", completedIds, !isPremium),
                    createMilestone("do-m6", "Container Security & Registry Management", "Vulnerability scanning, automated image tagging, and GitHub Container Registry (GHCR).", "6 hours", 75, List.of("Docker Security", "GHCR", "Trivy"), "Zero-vulnerability base image pipeline", completedIds, !isPremium)
            )));
            phases.add(createPhase("do-p4", 4, "Kubernetes Orchestration", "Pods, Deployments, Services & Ingress", !isPremium, List.of(
                    createMilestone("do-m7", "Kubernetes Core Architecture", "Control plane, Kubelet, Pods, Deployments, ReplicaSets, and rolling updates.", "12 hours", 95, List.of("Kubernetes", "K8s Deployments", "Rolling Updates"), "Zero-downtime deployment manifest for microservice", completedIds, !isPremium),
                    createMilestone("do-m8", "Services, Ingress & Helm Charts", "ClusterIP, NodePort, Ingress Controller, Helm chart packaging, and values customization.", "10 hours", 95, List.of("Helm", "Ingress", "K8s Services"), "Modular production Helm chart for cloud application", completedIds, !isPremium)
            )));
            phases.add(createPhase("do-p5", 5, "Infrastructure as Code (Terraform)", "Cloud Provisioning & State Management", !isPremium, List.of(
                    createMilestone("do-m9", "Terraform & AWS Cloud Architecture", "VPCs, subnets, EC2, RDS, S3, IAM roles, remote S3 state locks with DynamoDB.", "12 hours", 100, List.of("Terraform", "AWS", "IaC", "Cloud"), "Production AWS VPC and Kubernetes cluster via Terraform", completedIds, !isPremium)
            )));
            phases.add(createPhase("do-p6", 6, "Observability & Site Reliability", "Prometheus, Grafana, OpenTelemetry & Logging", !isPremium, List.of(
                    createMilestone("do-m10", "Metrics & Alerting with Prometheus & Grafana", "Custom metrics scraping, PromQL, alert rules, and latency/error dashboards.", "10 hours", 90, List.of("Prometheus", "Grafana", "Observability"), "Kubernetes cluster monitoring and P99 latency dashboard", completedIds, !isPremium),
                    createMilestone("do-m11", "Centralized Logging & Tracing", "Grafana Loki / ELK stack, distributed tracing with OpenTelemetry and Jaeger.", "8 hours", 85, List.of("Loki", "OpenTelemetry", "Logging"), "End-to-end distributed trace and log correlation system", completedIds, !isPremium)
            )));
            phases.add(createPhase("do-p7", 7, "Flagship GitOps Cloud Platform", "ArgoCD, Multi-Cluster & Automated Deployment", !isPremium, List.of(
                    createMilestone("do-m12", "Enterprise GitOps Pipeline with ArgoCD", "Declarative cluster sync, canary deployments with Argo Rollouts, and automated rollback.", "20 hours", 150, List.of("GitOps", "ArgoCD", "Kubernetes", "AWS"), "Automated GitOps Multi-Environment Cloud Platform", completedIds, !isPremium)
            )));
            phases.add(createPhase("do-p8", 8, "SRE & Cloud System Design Interviews", "Disaster Recovery, High Availability & Interviews", !isPremium, List.of(
                    createMilestone("do-m13", "SRE Architecture & Technical Interviews", "Multi-region failover, chaos engineering, SLI/SLO definitions, and AI Mock Interviews.", "12 hours", 100, List.of("SRE", "Cloud Architecture", "Interview Prep"), "Complete 5 Cloud/DevOps mock interviews on Samprepix", completedIds, !isPremium)
            )));
            return phases;
        }

        if ("mobile".equalsIgnoreCase(trackId)) {
            phases.add(createPhase("mob-p1", 1, "Flutter & Dart Fundamentals", "Language Syntax, OOP & Async Streams", false, List.of(
                    createMilestone("mob-m1", "Dart 3 Syntax & Language Features", "Null safety, records, patterns, extension methods, and async/await streams.", "8 hours", 60, List.of("Dart", "Null Safety", "Streams"), "Command-line interactive expense analyzer in Dart", completedIds),
                    createMilestone("mob-m2", "Flutter Widget Tree & Layouts", "Stateless vs Stateful, CustomPaint, responsive layouts, and Material 3 design.", "10 hours", 75, List.of("Flutter", "Widgets", "Material 3"), "Responsive multi-screen dashboard UI in Flutter", completedIds)
            )));
            phases.add(createPhase("mob-p2", 2, "DSA for Mobile & Problem Solving", "Algorithms & Performance in Mobile Context", false, List.of(
                    createMilestone("mob-m3", "Mobile Data Structures & Memory", "Lists, Sets, Maps, memory profiling, image caching, and list view recycling.", "10 hours", 80, List.of("DSA", "Dart", "Memory Optimization"), "High-performance infinite virtual scroll list in Flutter", completedIds),
                    createMilestone("mob-m4", "Coding Arena Mobile Sprint", "Solve 50+ DSA problems in the Samprepix Arena.", "15 hours", 100, List.of("Algorithms", "DP", "Time Complexity"), "Achieve 5-day streak in Coding Arena", completedIds)
            )));
            phases.add(createPhase("mob-p3", 3, "State Management & Clean Architecture", "Riverpod, Bloc & Separation of Concerns", !isPremium, List.of(
                    createMilestone("mob-m5", "Riverpod 2.0 / Bloc Architecture", "Providers, state notifiers, immutable state, dependency injection, and clean layers.", "12 hours", 90, List.of("Riverpod", "Bloc", "Clean Architecture"), "Production e-commerce shopping cart with Riverpod", completedIds, !isPremium),
                    createMilestone("mob-m6", "Offline Storage & SQLite / Hive", "Local database caching, key-value stores with Hive, and background sync.", "8 hours", 85, List.of("Hive", "SQLite", "Offline First"), "Offline-first notes app with conflict resolution", completedIds, !isPremium)
            )));
            phases.add(createPhase("mob-p4", 4, "Networking, REST & GraphQL", "API Consumption, Error Handling & Auth", !isPremium, List.of(
                    createMilestone("mob-m7", "Dio HTTP Client & JWT Interceptors", "Automatic token refresh, interceptors, request retry, and error handling.", "10 hours", 90, List.of("Dio", "REST", "JWT Auth"), "Secure authentication module with biometric login fallback", completedIds, !isPremium)
            )));
            phases.add(createPhase("mob-p5", 5, "Native Features & Hardware APIs", "Camera, Geolocation, Sensors & Notifications", !isPremium, List.of(
                    createMilestone("mob-m8", "Push Notifications with Firebase (FCM)", "FCM background message handlers, local notifications, and deep linking.", "8 hours", 85, List.of("FCM", "Firebase", "Deep Linking"), "Real-time delivery tracking app with push updates", completedIds, !isPremium),
                    createMilestone("mob-m9", "Camera, GPS & Biometrics", "Device sensors, barcode scanning, location services, and fingerprint/Face ID.", "8 hours", 85, List.of("Biometrics", "GPS", "Hardware APIs"), "Field surveyor app with GPS geotagging & camera upload", completedIds, !isPremium)
            )));
            phases.add(createPhase("mob-p6", 6, "App Performance, Security & CI/CD", "Fastlane, ProGuard & Play Store Prep", !isPremium, List.of(
                    createMilestone("mob-m10", "Automated Mobile CI/CD with Fastlane", "Automated code signing, keystore encryption, GitHub Actions build, and beta distribution.", "10 hours", 95, List.of("Fastlane", "CI/CD", "Mobile DevOps"), "Automated TestFlight and Play Store internal track deployment", completedIds, !isPremium)
            )));
            phases.add(createPhase("mob-p7", 7, "Flagship Production Mobile App", "Full-Featured Enterprise Mobile Solution", !isPremium, List.of(
                    createMilestone("mob-m11", "Enterprise AI Interview Practice Mobile App", "Live audio recording, offline mode, payment integration, and smooth animations.", "20 hours", 150, List.of("Flutter", "Mobile Architecture", "Firebase", "Full App"), "Samprepix AI Mobile Companion Application", completedIds, !isPremium)
            )));
            phases.add(createPhase("mob-p8", 8, "Mobile System Design & Interviews", "Architecture, App Size & Technical Screen", !isPremium, List.of(
                    createMilestone("mob-m12", "Mobile System Design & Mock Interviews", "Designing WhatsApp mobile client, offline sync architecture, and AI Mock Interviews.", "12 hours", 100, List.of("Mobile System Design", "Scalability", "Interview"), "Complete 5 mobile engineering mock interviews on Samprepix", completedIds, !isPremium)
            )));
            return phases;
        }

        // Default: Java Full Stack
        phases.add(createPhase("jf-p1", 1, "Foundation & Modern Java", "Core Java, OOP Principles & Streams API", false, List.of(
                createMilestone("jf-m1", "Java 17/21 Syntax & OOP", "Encapsulation, Inheritance, Polymorphism, Records, and Pattern Matching.", "6 hours", 50, List.of("Java 21", "OOP", "Clean Code"), "Object-oriented banking CLI with validation rules", completedIds),
                createMilestone("jf-m2", "Collections & Streams API", "List, Map, Set, Stream filtering, mapping, collectors, and concurrency.", "8 hours", 75, List.of("Java Collections", "Streams API", "Lambdas"), "High-throughput data filtering pipeline", completedIds)
        )));
        phases.add(createPhase("jf-p2", 2, "Data Structures & Algorithms", "Algorithm Optimization & Placement Problem Solving", false, List.of(
                createMilestone("jf-m3", "Linear & Non-Linear Structures", "Arrays, Strings, LinkedLists, Trees, Heaps, and Graphs in Java.", "12 hours", 80, List.of("DSA", "Trees", "Graphs"), "Binary Search Tree visualizer and balance tester", completedIds),
                createMilestone("jf-m4", "Coding Arena Placement Sprint", "Solve 50+ Easy, Medium, and Hard problems in the Samprepix Arena.", "15 hours", 100, List.of("Algorithms", "DP", "Time Complexity"), "Achieve 5-day streak in Coding Arena", completedIds)
        )));
        phases.add(createPhase("jf-p3", 3, "Database Engineering & JPA", "Relational Modeling, Hibernate & Spring Data", !isPremium, List.of(
                createMilestone("jf-m5", "MySQL & Relational Design", "Normalization, Complex Joins, Composite Indexes, and Query Plans.", "8 hours", 75, List.of("MySQL", "SQL", "Indexing"), "Database schema design for a multi-tenant university portal", completedIds, !isPremium),
                createMilestone("jf-m6", "Hibernate & Spring Data JPA", "Entity relationships, Lazy Loading, N+1 query problem, and transactions.", "10 hours", 85, List.of("Spring Data JPA", "Hibernate", "Transactions"), "Repository layer with optimized JPQL queries", completedIds, !isPremium)
        )));
        phases.add(createPhase("jf-p4", 4, "Spring Boot & RESTful Microservices", "Enterprise API Architecture & Validation", !isPremium, List.of(
                createMilestone("jf-m7", "Spring Boot 3 Core & Architecture", "Dependency Injection, Controllers, Services, DTOs, and global exception handling.", "10 hours", 90, List.of("Spring Boot", "REST APIs", "Validation"), "Production-grade RESTful API with OpenAPI/Swagger docs", completedIds, !isPremium),
                createMilestone("jf-m8", "Spring Security 6 & JWT", "SecurityFilterChain, stateless session policy, JWT token generation & filters.", "8 hours", 90, List.of("Spring Security", "JWT", "RBAC"), "Role-based authentication & authorization filter system", completedIds, !isPremium)
        )));
        phases.add(createPhase("jf-p5", 5, "Frontend with React & Modern UI", "Interactive UI, State & API Client", !isPremium, List.of(
                createMilestone("jf-m9", "React Architecture & Hooks", "Component composition, React Router 7, useEffect, and Axios interceptors.", "12 hours", 85, List.of("React", "JavaScript", "Tailwind CSS"), "Interactive recruiter management dashboard", completedIds, !isPremium)
        )));
        phases.add(createPhase("jf-p6", 6, "Caching, Messaging & Testing", "Performance Optimization & Automated Tests", !isPremium, List.of(
                createMilestone("jf-m10", "Redis Caching & JUnit 5 / Mockito", "Distributed caching, cache annotations, unit tests and MockMvc integration tests.", "8 hours", 85, List.of("Redis", "JUnit 5", "Mockito"), "Comprehensive test suite achieving 80%+ code coverage", completedIds, !isPremium)
        )));
        phases.add(createPhase("jf-p7", 7, "Flagship Full Stack Project", "Enterprise AI/Placement Platform", !isPremium, List.of(
                createMilestone("jf-m11", "Full Stack Microservices Architecture", "Complete end-to-end platform with payment gateway, auth, and automated background jobs.", "20 hours", 150, List.of("Full Stack", "Spring Boot", "React", "Docker"), "Flagship Campus Placement Management Suite", completedIds, !isPremium)
        )));
        phases.add(createPhase("jf-p8", 8, "System Design & Placement Interviews", "Tier-1 Readiness & High-Level Architecture", !isPremium, List.of(
                createMilestone("jf-m12", "High-Level System Design & Mock Mocks", "Load balancing, horizontal scaling, database sharding, and live AI Mock Interviews.", "12 hours", 100, List.of("System Design", "Microservices", "Interview Prep"), "Complete 5 AI Mock Interviews with >80% score", completedIds, !isPremium)
        )));

        return phases;
    }

    private List<RoadmapPhaseDto> buildPhasesForCustomTrack(String domainTitle, Set<String> completedIds, boolean isPremium) {
        String safeTitle = (domainTitle != null && !domainTitle.isBlank()) ? domainTitle.trim() : "Custom Engineering";
        String lower = safeTitle.toLowerCase();
        List<RoadmapPhaseDto> phases = new ArrayList<>();

        if (lower.contains("cyber") || lower.contains("security") || lower.contains("infosec") || lower.contains("pentest")) {
            phases.add(createPhase("sec-p1", 1, "Networking & Linux Fundamentals", "TCP/IP, Wireshark, Bash & Security Tooling", false, List.of(
                    createMilestone("sec-m1", "Network Protocols & Traffic Analysis", "Deep packet inspection, Wireshark analysis, DNS, HTTP/S, and TCP handshakes.", "6 hours", 60, List.of("Wireshark", "TCP/IP", "Networking"), "Network packet capture and port scanner script in Python", completedIds),
                    createMilestone("sec-m2", "Linux Hardening & Shell Scripting", "File permissions, SSH hardening, iptables firewall configuration, and Bash automation.", "8 hours", 75, List.of("Linux", "Bash", "Hardening"), "Automated Linux security baseline audit script", completedIds)
            )));
            phases.add(createPhase("sec-p2", 2, "Security Principles & Cryptography", "Symmetric, Asymmetric & Identity Management", false, List.of(
                    createMilestone("sec-m3", "Applied Cryptography & PKI", "AES, RSA, ECC, Hashing (SHA-256), Certificates, SSL/TLS, and Key Management.", "8 hours", 80, List.of("Cryptography", "PKI", "SSL/TLS"), "End-to-end encrypted messaging CLI tool", completedIds),
                    createMilestone("sec-m4", "Identity, IAM & Zero Trust Architecture", "OAuth 2.0, OpenID Connect, SAML, Multi-Factor Authentication, and Zero Trust models.", "10 hours", 90, List.of("IAM", "OAuth 2.0", "Zero Trust"), "Zero Trust access proxy with policy enforcement", completedIds)
            )));
            phases.add(createPhase("sec-p3", 3, "Web Application Security (OWASP Top 10)", "Vulnerability Analysis & Exploitation Defense", !isPremium, List.of(
                    createMilestone("sec-m5", "OWASP Top 10 Attack & Defense", "SQL Injection, Cross-Site Scripting (XSS), CSRF, SSRF, and Broken Access Control.", "10 hours", 85, List.of("OWASP", "Burp Suite", "AppSec"), "Vulnerable web application remediation showcase", completedIds, !isPremium),
                    createMilestone("sec-m6", "API Security & Token Validation", "JWT tampering, rate-limiting bypass, Mass Assignment, and CORS misconfiguration.", "8 hours", 85, List.of("API Security", "JWT", "REST"), "Automated API security testing and fuzzing suite", completedIds, !isPremium)
            )));
            phases.add(createPhase("sec-p4", 4, "Vulnerability Assessment & Pentesting", "Reconnaissance, Exploitation & Reporting", !isPremium, List.of(
                    createMilestone("sec-m7", "Network Reconnaissance & Vulnerability Scanning", "Nmap active discovery, Nessus vulnerability scanner, and banner grabbing.", "10 hours", 90, List.of("Nmap", "Nessus", "Recon"), "Complete enterprise attack surface assessment report", completedIds, !isPremium),
                    createMilestone("sec-m8", "Metasploit Framework & Privilege Escalation", "Exploit modules, payload delivery, Linux/Windows privilege escalation vectors.", "12 hours", 100, List.of("Metasploit", "PrivEsc", "Exploitation"), "Privilege escalation walkthrough on HackTheBox lab", completedIds, !isPremium)
            )));
            phases.add(createPhase("sec-p5", 5, "Cloud Security & Container Hardening", "AWS/GCP IAM, Kubernetes & Docker Security", !isPremium, List.of(
                    createMilestone("sec-m9", "Container Security with Docker & Trivy", "Rootless containers, immutable images, vulnerability scanning with Trivy.", "8 hours", 85, List.of("Docker", "Trivy", "Container Security"), "Hardened multi-stage Dockerfile pipeline with zero CVEs", completedIds, !isPremium),
                    createMilestone("sec-m10", "Cloud Security Posture Management (CSPM)", "AWS GuardDuty, Security Hub, IAM principle of least privilege, and S3 bucket security.", "10 hours", 90, List.of("AWS Security", "CSPM", "IAM"), "Automated AWS CloudTrail anomaly detection bot", completedIds, !isPremium)
            )));
            phases.add(createPhase("sec-p6", 6, "SOC, SIEM & Incident Response", "Threat Detection, Log Analysis & Threat Hunting", !isPremium, List.of(
                    createMilestone("sec-m11", "SIEM Architecture with Splunk / Elastic", "Log ingestion, correlation rules, dashboard construction, and anomaly alerts.", "10 hours", 95, List.of("SIEM", "Splunk", "Elasticsearch"), "Brute force attack detection & alerting dashboard in Elastic SIEM", completedIds, !isPremium)
            )));
            phases.add(createPhase("sec-p7", 7, "Flagship Security Capstone Project", "End-to-End Penetration Test & Audit", !isPremium, List.of(
                    createMilestone("sec-m12", "Full-Scope Penetration Test & Remediated Architecture", "Comprehensive black-box pentest, formal CVE audit report, and hardened production infrastructure.", "20 hours", 150, List.of("CapStone", "Audit Report", "Pentesting"), "Enterprise Penetration Test & Security Architecture Whitepaper", completedIds, !isPremium)
            )));
            phases.add(createPhase("sec-p8", 8, "Security Engineering System Design", "Threat Modeling & Technical Interview Prep", !isPremium, List.of(
                    createMilestone("sec-m13", "STRIDE Threat Modeling & Placement Technical Screens", "Designing secure banking gateways, data residency compliance, and mock technical interviews.", "12 hours", 100, List.of("Threat Modeling", "STRIDE", "Interview"), "Complete 5 security architecture mock interviews on Samprepix", completedIds, !isPremium)
            )));
            return phases;
        }

        if (lower.contains("data") || lower.contains("analyst") || lower.contains("analytics") || lower.contains("bi")) {
            phases.add(createPhase("da-p1", 1, "Data Foundations & Advanced SQL", "PostgreSQL, Window Functions & CTEs", false, List.of(
                    createMilestone("da-m1", "Complex SQL for Data Analysis", "Window functions (ROW_NUMBER, RANK, LEAD, LAG), CTEs, Grouping sets, and subqueries.", "8 hours", 65, List.of("SQL", "PostgreSQL", "Window Functions"), "Customer cohort retention analysis in PostgreSQL", completedIds),
                    createMilestone("da-m2", "Data Cleaning with Python & Pandas", "Handling missing values, outlier detection, regex parsing, and datetime manipulations.", "8 hours", 75, List.of("Python", "Pandas", "NumPy"), "Real-world dirty dataset cleaning and normalization pipeline", completedIds)
            )));
            phases.add(createPhase("da-p2", 2, "Exploratory Data Analysis & Statistics", "Hypothesis Testing & Visual Storytelling", false, List.of(
                    createMilestone("da-m3", "Inferential Statistics & A/B Testing", "P-values, confidence intervals, t-tests, chi-squared tests, and A/B test design.", "8 hours", 80, List.of("Statistics", "A/B Testing", "Hypothesis Testing"), "A/B test statistical significance calculator & report", completedIds),
                    createMilestone("da-m4", "Data Visualization with Seaborn & Plotly", "Interactive distributions, heatmaps, box plots, and executive charts.", "6 hours", 75, List.of("Plotly", "Seaborn", "Visualization"), "Interactive macroeconomic trends dashboard in Plotly", completedIds)
            )));
            phases.add(createPhase("da-p3", 3, "Business Intelligence & Dashboards", "Power BI, Tableau & Executive Metrics", !isPremium, List.of(
                    createMilestone("da-m5", "Interactive Executive Dashboard in Power BI", "Star schema modeling, DAX measures, KPI drill-downs, and automated refresh.", "10 hours", 85, List.of("Power BI", "DAX", "Data Modeling"), "SaaS revenue churn & customer acquisition dashboard", completedIds, !isPremium)
            )));
            phases.add(createPhase("da-p4", 4, "Data Warehousing & ETL Pipelines", "dbt, Snowflake & Airflow Automation", !isPremium, List.of(
                    createMilestone("da-m6", "Modern Data Stack with dbt & Snowflake", "Dimension modeling, SCD Type 2, dbt tests, incremental models, and transformations.", "10 hours", 90, List.of("dbt", "Snowflake", "Data Warehouse"), "Complete dbt transformation pipeline on e-commerce clickstream data", completedIds, !isPremium)
            )));
            phases.add(createPhase("da-p5", 5, "Predictive Analytics & Machine Learning", "Scikit-Learn, Regression & Clustering", !isPremium, List.of(
                    createMilestone("da-m7", "Customer Segmentation & Churn Prediction", "Random Forest, Logistic Regression, K-Means clustering, and feature importance.", "10 hours", 90, List.of("Scikit-Learn", "Machine Learning", "Clustering"), "Predictive customer churn model with actionable business recommendations", completedIds, !isPremium)
            )));
            phases.add(createPhase("da-p6", 6, "Big Data Analytics with PySpark", "Distributed Dataframes & Spark SQL", !isPremium, List.of(
                    createMilestone("da-m8", "Large-Scale Data Processing with PySpark", "Spark sessions, transformations, actions, caching, and partitioning optimization.", "10 hours", 95, List.of("PySpark", "Big Data", "Distributed Computing"), "Analyze 10M+ rows of taxi ride data with PySpark", completedIds, !isPremium)
            )));
            phases.add(createPhase("da-p7", 7, "Flagship End-to-End Analytics Project", "Full Analytics Architecture & Insight Delivery", !isPremium, List.of(
                    createMilestone("da-m9", "Enterprise Analytics & Business Strategy Suite", "Raw data ingestion, automated ETL, warehouse modeling, and interactive executive reporting.", "20 hours", 150, List.of("Full Stack Analytics", "ETL", "BI", "Machine Learning"), "Marketplace Operations Intelligence Platform", completedIds, !isPremium)
            )));
            phases.add(createPhase("da-p8", 8, "Data Systems Design & Interviews", "Metric Design, Case Studies & Technical Screens", !isPremium, List.of(
                    createMilestone("da-m10", "Business Case Studies & SQL Interview Mastery", "System design for analytics, metric definition frameworks, and live SQL technical rounds.", "12 hours", 100, List.of("Case Studies", "SQL Interviews", "System Design"), "Complete 5 data analyst technical mock interviews on Samprepix", completedIds, !isPremium)
            )));
            return phases;
        }

        // Generic / Tailored Custom Track
        phases.add(createPhase("cust-p1", 1, "Foundations & Core Principles of " + safeTitle, "Essential Tools, Environment & Architecture Setup", false, List.of(
                createMilestone("cust-m1", safeTitle + " Foundations & Standards", "Master the core programming patterns, tooling, and development environment for " + safeTitle + ".", "8 hours", 60, List.of(safeTitle, "Foundations", "Clean Code"), "Set up an automated modern development workspace for " + safeTitle, completedIds),
                createMilestone("cust-m2", "Git, CLI & Workflow Automation", "Version control best practices, CI pipelines, and command-line efficiency.", "6 hours", 65, List.of("Git", "Automation", "CI/CD"), "Publish a reproducible project skeleton on GitHub", completedIds)
        )));
        phases.add(createPhase("cust-p2", 2, "Data Structures & Algorithmic Problem Solving", "Core Algorithms & Placement Readiness", false, List.of(
                createMilestone("cust-m3", "Core DSA & Placement Foundations", "Arrays, Strings, HashMaps, Two-Pointers, and Search/Sort algorithms.", "12 hours", 80, List.of("DSA", "Algorithms", "Problem Solving"), "Solve 40+ curated placement challenges on Samprepix Coding Arena", completedIds),
                createMilestone("cust-m4", "Domain-Specific Architecture Patterns", "Architectural paradigms, design patterns, and idiomatic idioms in " + safeTitle + ".", "8 hours", 85, List.of("Architecture", "Design Patterns", "Clean Code"), "Modular architectural design prototype with test suite", completedIds)
        )));
        phases.add(createPhase("cust-p3", 3, "Core Engineering & Database Systems", "Data Persistence, Modeling & State Management", !isPremium, List.of(
                createMilestone("cust-m5", "Database Design & Storage Architecture", "Relational/NoSQL database modeling, querying efficiency, and caching layers.", "10 hours", 85, List.of("Databases", "SQL/NoSQL", "Data Modeling"), "Optimized data persistence layer for " + safeTitle, completedIds, !isPremium)
        )));
        phases.add(createPhase("cust-p4", 4, "High-Performance APIs & Networking", "Services, Protocols & Communication", !isPremium, List.of(
                createMilestone("cust-m6", "API Integration & Real-Time Networking", "REST/GraphQL/gRPC APIs, asynchronous handling, error boundaries, and telemetry.", "10 hours", 90, List.of("APIs", "Networking", "Async"), "High-concurrency service layer with rate limiting and logging", completedIds, !isPremium)
        )));
        phases.add(createPhase("cust-p5", 5, "Performance Optimization & Observability", "Profiling, Benchmarking & Memory Optimization", !isPremium, List.of(
                createMilestone("cust-m7", "Profiling & System Optimization", "CPU/Memory profiling, bottleneck analysis, caching, and latency reduction.", "8 hours", 90, List.of("Optimization", "Profiling", "Performance"), "Benchmark and optimize critical system paths by 40%+", completedIds, !isPremium)
        )));
        phases.add(createPhase("cust-p6", 6, "Security, Testing Automation & DevOps", "Automated Testing, Docker & Security Hardening", !isPremium, List.of(
                createMilestone("cust-m8", "Automated Test Suite & Container Deployment", "Unit tests, integration tests, Docker containerization, and automated GitHub Actions.", "10 hours", 95, List.of("Docker", "CI/CD", "Testing"), "Production-ready automated deployment pipeline", completedIds, !isPremium)
        )));
        phases.add(createPhase("cust-p7", 7, "Flagship Production Portfolio Project", "Enterprise-Ready Full Scope Implementation", !isPremium, List.of(
                createMilestone("cust-m9", "Flagship " + safeTitle + " Production Platform", "A comprehensive, enterprise-level application demonstrating mastery of " + safeTitle + ".", "20 hours", 150, List.of("Production Project", "Enterprise", safeTitle), "Flagship " + safeTitle + " Showcase Application", completedIds, !isPremium)
        )));
        phases.add(createPhase("cust-p8", 8, "System Design & Placement Mock Interviews", "Tier-1 Technical Rounds & System Design Mastery", !isPremium, List.of(
                createMilestone("cust-m10", "System Architecture & Mock Placement Rounds", "Scalability, fault tolerance, trade-off analysis, and technical interview screens.", "12 hours", 100, List.of("System Design", "Scalability", "Mock Interview"), "Complete 5 specialized mock interviews for " + safeTitle + " on Samprepix", completedIds, !isPremium)
        )));

        return phases;
    }

    private RoadmapPhaseDto createPhase(String phaseId, int number, String title, String subtitle, boolean locked, List<MilestoneDto> milestones) {
        return RoadmapPhaseDto.builder()
                .phaseId(phaseId)
                .phaseNumber(number)
                .title(title)
                .subtitle(subtitle)
                .locked(locked)
                .milestones(milestones)
                .build();
    }

    private MilestoneDto createMilestone(
            String id, String title, String desc, String time, int xp, List<String> skills,
            String project, Set<String> completedIds
    ) {
        return createMilestone(id, title, desc, time, xp, skills, project, completedIds, false);
    }

    private MilestoneDto createMilestone(
            String id, String title, String desc, String time, int xp, List<String> skills,
            String project, Set<String> completedIds, boolean locked
    ) {
        return MilestoneDto.builder()
                .id(id)
                .title(title)
                .description(desc)
                .estimatedTime(time)
                .xp(xp)
                .skills(skills)
                .recommendedProject(project)
                .completed(completedIds.contains(id))
                .locked(locked)
                .build();
    }
}
