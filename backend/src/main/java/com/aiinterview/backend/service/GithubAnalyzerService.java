package com.aiinterview.backend.service;

import com.aiinterview.backend.dto.github.GithubAnalysisResponse;
import com.aiinterview.backend.dto.github.GithubAnalysisResponse.*;
import com.aiinterview.backend.entity.GithubAnalysisResult;
import com.aiinterview.backend.entity.ResumeAnalysis;
import com.aiinterview.backend.entity.User;
import com.aiinterview.backend.entity.UserProfile;
import com.aiinterview.backend.repository.CodingProblemCompletionRepository;
import com.aiinterview.backend.repository.GithubAnalysisResultRepository;
import com.aiinterview.backend.repository.ResumeAnalysisRepository;
import com.aiinterview.backend.repository.UserProfileRepository;
import com.aiinterview.backend.entity.GitHubConnection;
import com.aiinterview.backend.repository.GitHubConnectionRepository;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubAnalyzerService {

    private static final Pattern GITHUB_PROFILE_URL_PATTERN =
            Pattern.compile("^https://(?:www\\.)?github\\.com/([a-zA-Z0-9](?:[a-zA-Z0-9]|-(?=[a-zA-Z0-9])){0,38})/?$", Pattern.CASE_INSENSITIVE);

    private static final Set<String> RESERVED_GITHUB_NAMES = Set.of(
            "about", "features", "pricing", "security", "login", "join", "enterprise",
            "explore", "marketplace", "sponsors", "settings", "notifications", "contact",
            "terms", "privacy", "organizations", "search", "trending", "stars"
    );

    private final GithubAnalysisResultRepository analysisResultRepository;
    private final UserProfileRepository userProfileRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final CodingProblemCompletionRepository completionRepository;
    private final EntitlementService entitlementService;
    private final GitHubConnectionRepository gitHubConnectionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Validates GitHub profile URL against SSRF and syntax rules.
     */
    public String validateAndExtractUsername(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("GitHub Profile URL cannot be empty.");
        }
        String trimmed = url.trim();

        if (trimmed.contains("@")) {
            throw new IllegalArgumentException("Invalid GitHub URL format: userinfo is not allowed.");
        }

        int qIdx = trimmed.indexOf('?');
        if (qIdx >= 0) {
            trimmed = trimmed.substring(0, qIdx);
        }
        int fIdx = trimmed.indexOf('#');
        if (fIdx >= 0) {
            trimmed = trimmed.substring(0, fIdx);
        }

        Matcher matcher = GITHUB_PROFILE_URL_PATTERN.matcher(trimmed);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Please enter a valid GitHub profile URL (e.g. https://github.com/username).");
        }

        String username = matcher.group(1);
        if (RESERVED_GITHUB_NAMES.contains(username.toLowerCase())) {
            throw new IllegalArgumentException("'" + username + "' is a reserved GitHub keyword, not an individual user profile.");
        }

        return username;
    }

    /**
     * Main analysis method. Fetches public GitHub data, enriches with Samprepix user data,
     * scores the profile deterministically, and applies premium gating.
     */
    @Transactional
    public GithubAnalysisResponse analyzeProfile(User user, String profileUrl) {
        String username = validateAndExtractUsername(profileUrl);
        boolean isPremium = entitlementService.hasPremiumAccess(user);
        String effectivePlan = entitlementService.getEffectivePlan(user);

        // 1. Fetch public profile from GitHub API
        JsonNode userJson = fetchGitHubUser(username);

        // 2. Fetch public repositories
        JsonNode reposJson = fetchGitHubRepos(username);

        // 3. Gather Samprepix context (only for the user's own profile to prevent cross-account contamination)
        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
        boolean isOwnProfile = false;
        if (profile != null && profile.getGithubUrl() != null && !profile.getGithubUrl().isBlank()) {
            String pGh = profile.getGithubUrl().trim().toLowerCase(Locale.ROOT);
            if (pGh.endsWith("/" + username.toLowerCase(Locale.ROOT)) || pGh.equals(username.toLowerCase(Locale.ROOT))) {
                isOwnProfile = true;
            }
        }
        if (!isOwnProfile && user != null) {
            Optional<GitHubConnection> conn = gitHubConnectionRepository.findByUser(user);
            if (conn.isPresent()) {
                String connectedGh = fetchAuthenticatedGitHubUsername(conn.get().getAccessToken());
                if (username.equalsIgnoreCase(connectedGh)) {
                    isOwnProfile = true;
                }
            }
        }

        UserProfile profileToUse = isOwnProfile ? profile : null;
        String userEmail = user.getEmail() != null ? user.getEmail().trim() : "";
        List<ResumeAnalysis> resumeAnalyses = (isOwnProfile && !userEmail.isBlank()) ? resumeAnalysisRepository.findByUserEmail(userEmail) : List.of();
        ResumeAnalysis latestResume = (resumeAnalyses != null && !resumeAnalyses.isEmpty()) ? resumeAnalyses.get(0) : null;

        // 4. Aggregate Technical Signals (Priority 1: GitHub, Priority 2: Samprepix)
        Map<String, Integer> languageCounts = new LinkedHashMap<>();
        List<RepoInfo> repoList = new ArrayList<>();

        int totalStars = 0;
        int totalForks = 0;
        int reposWithDescription = 0;

        if (reposJson != null && reposJson.isArray()) {
            for (JsonNode r : reposJson) {
                if (r.path("fork").asBoolean(false)) {
                    continue; // Focus on original projects
                }
                String name = r.path("name").asText("");
                String desc = r.path("description").asText("");
                String lang = r.path("language").asText("");
                String htmlUrl = r.path("html_url").asText("");
                int stars = r.path("stargazers_count").asInt(0);
                int forks = r.path("forks_count").asInt(0);
                String homepage = r.path("homepage").asText("").trim();
                List<String> topics = new ArrayList<>();
                JsonNode topicsNode = r.path("topics");
                if (topicsNode.isArray()) {
                    for (JsonNode t : topicsNode) {
                        String topicText = t.asText("").trim();
                        if (!topicText.isBlank()) {
                            topics.add(topicText);
                        }
                    }
                }

                totalStars += stars;
                totalForks += forks;

                if (!desc.isBlank()) {
                    reposWithDescription++;
                }

                if (!lang.isBlank()) {
                    languageCounts.put(lang, languageCounts.getOrDefault(lang, 0) + 1);
                }

                repoList.add(new RepoInfo(name, htmlUrl, desc, lang, stars, forks, homepage, topics));
            }
        }

        // Sort repos by stars, then descriptions, then repository name (deterministic tie-breaker)
        repoList.sort((a, b) -> {
            if (b.stars != a.stars) return Integer.compare(b.stars, a.stars);
            if (b.description.length() != a.description.length()) return Integer.compare(b.description.length(), a.description.length());
            return a.name.compareToIgnoreCase(b.name);
        });

        // Top languages list
        List<Map<String, Object>> topLanguages = new ArrayList<>();
        int totalLangRepos = languageCounts.values().stream().mapToInt(Integer::intValue).sum();
        languageCounts.entrySet().stream()
                .sorted((e1, e2) -> {
                    int cmp = Integer.compare(e2.getValue(), e1.getValue());
                    if (cmp != 0) return cmp;
                    return e1.getKey().compareToIgnoreCase(e2.getKey());
                })
                .limit(6)
                .forEach(e -> {
                    int pct = totalLangRepos > 0 ? (int) Math.round((e.getValue() * 100.0) / totalLangRepos) : 0;
                    topLanguages.add(Map.of("name", e.getKey(), "count", e.getValue(), "percentage", pct));
                });

        // Secondary source: If GitHub languages are sparse, incorporate Samprepix verified skills
        List<String> combinedSkills = new ArrayList<>();
        languageCounts.keySet().forEach(combinedSkills::add);

        if (profileToUse != null && profileToUse.getSkills() != null) {
            for (String s : profileToUse.getSkills()) {
                if (!combinedSkills.contains(s) && combinedSkills.size() < 12) {
                    combinedSkills.add(s);
                }
            }
        }

        // CV Fallback: If skills are sparse, extract real skills from user's uploaded ResumeAnalysis
        if (latestResume != null && latestResume.getSkills() != null && !latestResume.getSkills().isBlank()) {
            String[] resumeSkillTokens = latestResume.getSkills().split("[,;|•\n]+");
            for (String tok : resumeSkillTokens) {
                String trimmed = tok.trim();
                if (!trimmed.isBlank() && !combinedSkills.contains(trimmed) && combinedSkills.size() < 12) {
                    combinedSkills.add(trimmed);
                }
            }
        }

        // Factual Target Role derivation:
        String targetRole = (profileToUse != null && profileToUse.getTargetRole() != null && !profileToUse.getTargetRole().isBlank())
                ? profileToUse.getTargetRole().trim()
                : null;

        if (targetRole == null || targetRole.isBlank() || "Software Engineer".equalsIgnoreCase(targetRole)) {
            String primaryLang = !topLanguages.isEmpty() ? String.valueOf(topLanguages.get(0).get("name")).toLowerCase(Locale.ROOT) : "";
            if (primaryLang.contains("java") && !primaryLang.contains("script")) {
                targetRole = "Java Software Engineer";
            } else if (primaryLang.contains("python")) {
                targetRole = "Python & Backend Developer";
            } else if (primaryLang.contains("script") || primaryLang.contains("typescript") || primaryLang.contains("react") || primaryLang.contains("vue") || primaryLang.contains("node")) {
                targetRole = "Full Stack Developer";
            } else if (primaryLang.contains("c") || primaryLang.contains("rust") || primaryLang.contains("go")) {
                targetRole = "Systems & Software Engineer";
            } else {
                targetRole = "Software Engineer";
            }
        }

        // 5. Deterministic Category Scoring (0 to 100 across 4 core pillars)
        String ghName = userJson.path("name").asText("");
        String ghBio = userJson.path("bio").asText("");
        String ghLocation = userJson.path("location").asText("");
        String ghCompany = userJson.path("company").asText("");
        String ghBlog = userJson.path("blog").asText("");
        String ghAvatar = userJson.path("avatar_url").asText("");
        int publicRepos = userJson.path("public_repos").asInt(0);
        int followers = userJson.path("followers").asInt(0);
        int following = userJson.path("following").asInt(0);

        List<String> deductions = new ArrayList<>();
        List<String> improvements = new ArrayList<>();

        // Category 1: Profile Completeness (Max 25)
        int scoreProfile = 0;
        if (!ghName.isBlank()) scoreProfile += 5;
        if (!ghAvatar.isBlank()) scoreProfile += 5;
        if (!ghBio.isBlank()) {
            scoreProfile += 8;
            if (ghBio.length() < 20) {
                deductions.add("Brief bio: Your GitHub bio is very short. Expand it with your target role and core stack.");
            }
        } else {
            deductions.add("Missing GitHub bio: Recruiters scan your bio within 5 seconds of opening your profile.");
            improvements.add("Add a concise professional bio highlighting: '" + targetRole + " specializing in " + (combinedSkills.isEmpty() ? "modern full-stack development" : String.join(", ", combinedSkills.subList(0, Math.min(3, combinedSkills.size())))) + ".'");
        }
        if (!ghLocation.isBlank()) scoreProfile += 4;
        if (!ghBlog.isBlank() || !ghCompany.isBlank()) scoreProfile += 3;
        scoreProfile = Math.min(25, Math.max(4, scoreProfile));

        // Category 2: Repository Quality & Diversity (Max 30)
        int scoreProjects = 0;
        if (publicRepos > 0) scoreProjects += 7;
        if (publicRepos >= 3) scoreProjects += 7;
        if (reposWithDescription >= 2) scoreProjects += 8;
        if (totalStars > 0 || totalForks > 0) scoreProjects += 5;
        if (topLanguages.size() >= 2) scoreProjects += 3;

        if (publicRepos == 0) {
            deductions.add("Zero public repositories: Recruiters cannot inspect your code or problem-solving ability.");
            improvements.add("Publish at least 2 complete, well-structured projects showcasing your " + targetRole + " skills.");
        } else if (reposWithDescription < Math.min(3, repoList.size())) {
            deductions.add("Missing repository descriptions: " + (repoList.size() - reposWithDescription) + " of your public repositories have no summary.");
            improvements.add("Add 1-2 sentence descriptions to all public repositories specifying the project purpose and tech stack.");
        }
        scoreProjects = Math.min(30, Math.max(3, scoreProjects));

        // Category 3: Documentation & Setup (Max 20)
        int scoreDocumentation = 0;
        if (reposWithDescription > 0) scoreDocumentation += 7;
        if (reposWithDescription >= Math.max(1, repoList.size() / 2)) scoreDocumentation += 7;
        if (publicRepos >= 2) scoreDocumentation += 6;
        if (reposWithDescription < repoList.size() / 2 && !repoList.isEmpty()) {
            deductions.add("Low documentation coverage: Most repositories lack comprehensive setup instructions.");
            improvements.add("Include prerequisites, installation commands, environment variable guides, and screenshots in project descriptions and repositories.");
        }
        scoreDocumentation = Math.min(20, Math.max(2, scoreDocumentation));

        // Category 4: Professional Presentation & Recruiter Readability (Max 25)
        int scorePresentation = 0;
        if (!combinedSkills.isEmpty()) scorePresentation += 8;
        if (!ghName.isBlank() && !ghAvatar.isBlank()) scorePresentation += 7;
        if (!ghBio.isBlank()) scorePresentation += 6;
        if (publicRepos >= 3) scorePresentation += 4;
        scorePresentation = Math.min(25, Math.max(3, scorePresentation));

        // Evidence-based deterministic score directly derived from verified GitHub metrics (sum of 4 categories = max 100)
        int overallScore = scoreProfile + scoreProjects + scoreDocumentation + scorePresentation;
        overallScore = Math.min(100, Math.max(0, overallScore));

        // Category DTOs
        List<CategoryScoreDto> categoryScores = List.of(
                new CategoryScoreDto("Profile Completeness", scoreProfile, 25, scoreProfile >= 20 ? "Strong profile identity & credentials" : "Profile details need attention"),
                new CategoryScoreDto("Repository Quality & Diversity", scoreProjects, 30, scoreProjects >= 24 ? "Solid portfolio of active code repositories" : "Expand repository variety & descriptions"),
                new CategoryScoreDto("Documentation & Setup", scoreDocumentation, 20, scoreDocumentation >= 16 ? "Good technical clarity and descriptions" : "Add setup steps, tech stack & demo links"),
                new CategoryScoreDto("Recruiter Readability", scorePresentation, 25, scorePresentation >= 20 ? "Fast, credible technical impression for recruiters" : "Optimize for 30-second recruiter scans")
        );

        // 6. Repository Specific Recommendations
        List<RepoAnalysisDto> repoAnalyses = new ArrayList<>();
        int count = 0;
        for (RepoInfo repo : repoList) {
            if (count++ >= 5) break;
            List<String> recs = new ArrayList<>();
            if (repo.description.isBlank()) {
                recs.add("Add a concise description specifying problem solved and architecture.");
            }
            if (repo.stars == 0) {
                recs.add("Pin this repository to your profile to highlight it to hiring managers.");
            }
            recs.add("Include clean project documentation with Architecture Diagram, Setup steps, and Live Demo link.");
            recs.add("Add relevant GitHub Topics (" + (repo.language.isBlank() ? "web, api" : repo.language.toLowerCase() + ", fullstack") + ") for discoverability.");

            repoAnalyses.add(new RepoAnalysisDto(
                    repo.name,
                    repo.htmlUrl,
                    repo.description.isBlank() ? "No description provided." : repo.description,
                    repo.language.isBlank() ? "Multi-language" : repo.language,
                    repo.stars,
                    repo.forks,
                    recs
            ));
        }

        // 7. Recruiter View
        RecruiterViewDto recruiterView = buildRecruiterView(username, ghBio, publicRepos, topLanguages, combinedSkills, targetRole, totalStars);

        // 8. Persist analysis result
        try {
            GithubAnalysisResult result = GithubAnalysisResult.builder()
                    .user(user)
                    .githubUsername(username)
                    .profileUrl(profileUrl)
                    .avatarUrl(ghAvatar)
                    .bio(ghBio)
                    .publicRepos(publicRepos)
                    .followers(followers)
                    .following(following)
                    .overallScore(overallScore)
                    .categoryScoresJson(objectMapper.writeValueAsString(categoryScores))
                    .deductionsJson(objectMapper.writeValueAsString(deductions))
                    .improvementsJson(objectMapper.writeValueAsString(improvements))
                    .topLanguagesJson(objectMapper.writeValueAsString(topLanguages))
                    .currentReadme(null)
                    .recommendedReadme(null)
                    .readmeDiffJson(null)
                    .repoAnalysesJson(objectMapper.writeValueAsString(repoAnalyses))
                    .recruiterView(objectMapper.writeValueAsString(recruiterView))
                    .analyzedAt(LocalDateTime.now())
                    .build();
            analysisResultRepository.save(result);
        } catch (Exception e) {
            log.error("Failed to persist GitHub analysis result: {}", e.getMessage());
        }

        // 9. Build Response (applying free preview gating if not premium)
        return buildResponse(
                username, ghName, ghAvatar, profileUrl, ghBio, ghLocation, ghCompany, ghBlog,
                publicRepos, followers, following, overallScore, categoryScores, deductions, improvements,
                topLanguages, repoAnalyses, recruiterView, isPremium, effectivePlan
        );
    }

    /**
     * Retrieves the latest cached analysis for the user.
     */
    @Transactional(readOnly = true)
    public Optional<GithubAnalysisResponse> getLatestAnalysis(User user) {
        return analysisResultRepository.findFirstByUserOrderByAnalyzedAtDesc(user).map(result -> {
            boolean isPremium = entitlementService.hasPremiumAccess(user);
            String effectivePlan = entitlementService.getEffectivePlan(user);

            try {
                List<CategoryScoreDto> categoryScores = objectMapper.readValue(result.getCategoryScoresJson(), new TypeReference<>() {});
                List<String> deductions = objectMapper.readValue(result.getDeductionsJson(), new TypeReference<>() {});
                List<String> improvements = objectMapper.readValue(result.getImprovementsJson(), new TypeReference<>() {});
                List<Map<String, Object>> topLanguages = objectMapper.readValue(result.getTopLanguagesJson(), new TypeReference<>() {});
                List<RepoAnalysisDto> repoAnalyses = objectMapper.readValue(result.getRepoAnalysesJson(), new TypeReference<>() {});
                RecruiterViewDto recruiterView = objectMapper.readValue(result.getRecruiterView(), RecruiterViewDto.class);

                return buildResponse(
                        result.getGithubUsername(),
                        result.getGithubUsername(),
                        result.getAvatarUrl(),
                        result.getProfileUrl(),
                        result.getBio(),
                        "", "", "",
                        result.getPublicRepos(),
                        result.getFollowers(),
                        result.getFollowing(),
                        result.getOverallScore(),
                        categoryScores,
                        deductions,
                        improvements,
                        topLanguages,
                        repoAnalyses,
                        recruiterView,
                        isPremium,
                        effectivePlan
                );
            } catch (Exception e) {
                log.error("Error deserializing cached analysis: {}", e.getMessage());
                return null;
            }
        });
    }

    private GithubAnalysisResponse buildResponse(
            String username, String name, String avatarUrl, String profileUrl, String bio, String location,
            String company, String blog, int publicRepos, int followers, int following, int overallScore,
            List<CategoryScoreDto> categoryScores, List<String> deductions, List<String> improvements,
            List<Map<String, Object>> topLanguages, List<RepoAnalysisDto> repoAnalyses,
            RecruiterViewDto recruiterView, boolean isPremium, String effectivePlan
    ) {
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

        return GithubAnalysisResponse.builder()
                .username(username)
                .name(name)
                .avatarUrl(avatarUrl)
                .profileUrl(profileUrl)
                .bio(bio)
                .location(location)
                .company(company)
                .blog(blog)
                .publicRepos(publicRepos)
                .followers(followers)
                .following(following)
                .overallScore(overallScore)
                .categoryScores(categoryScores)
                .deductions(deductions)
                .improvements(improvements)
                .topLanguages(topLanguages)
                .repoAnalyses(repoAnalyses)
                .recruiterView(recruiterView)
                .premium(isPremium)
                .effectivePlan(effectivePlan)
                .analyzedAt(formattedDate)
                .build();
    }

    private JsonNode fetchGitHubUser(String username) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/users/" + username))
                    .header("User-Agent", "Samprepix-Platform")
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                throw new IllegalArgumentException("GitHub user '" + username + "' was not found. Please check the profile URL.");
            }
            if (response.statusCode() == 403 || response.statusCode() == 429) {
                throw new IllegalStateException("GitHub API rate limit reached. Please try again in a few minutes.");
            }
            if (response.statusCode() >= 500) {
                throw new IllegalStateException("GitHub is currently experiencing service disruption. Please try again later.");
            }
            if (response.statusCode() != 200) {
                throw new IllegalArgumentException("Unable to analyze profile (GitHub status: " + response.statusCode() + ").");
            }
            return objectMapper.readTree(response.body());
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling GitHub user API for {}: {}", username, e.getMessage());
            throw new IllegalStateException("Unable to connect to GitHub. Please check your network connection or try again later.");
        }
    }

    private JsonNode fetchGitHubRepos(String username) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/users/" + username + "/repos?sort=pushed&per_page=100"))
                    .header("User-Agent", "Samprepix-Platform")
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 403 || response.statusCode() == 429) {
                log.warn("GitHub API rate limit reached when fetching repos for {}", username);
                throw new IllegalStateException("GitHub API rate limit reached. Please try again in a few minutes.");
            }
            if (response.statusCode() == 200) {
                return objectMapper.readTree(response.body());
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to fetch repositories for {}: {}", username, e.getMessage());
        }
        return objectMapper.createArrayNode();
    }

    private String fetchAuthenticatedGitHubUsername(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/user"))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Samprepix-Platform")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode node = objectMapper.readTree(response.body());
                return node.path("login").asText(null);
            }
        } catch (Exception e) {
            log.error("Failed to verify authenticated GitHub user: {}", e.getMessage());
        }
        return null;
    }

    private RecruiterViewDto buildRecruiterView(
            String username, String bio, int publicRepos, List<Map<String, Object>> topLangs,
            List<String> combinedSkills, String targetRole, int totalStars
    ) {
        String immediate = "A tech recruiter reviewing this profile in 30 seconds sees "
                + (publicRepos > 0 ? publicRepos + " public repositories" : "no public code")
                + " with primary focus on "
                + (topLangs.isEmpty() ? "unspecified technologies" : topLangs.get(0).get("name"))
                + ". "
                + (!bio.isBlank() ? "The profile includes a concise professional bio." : "No professional bio is provided.");

        List<String> demonstrated = new ArrayList<>();
        topLangs.forEach(l -> demonstrated.add("Active code in " + l.get("name") + " (" + l.get("percentage") + "% of repositories)"));
        if (!combinedSkills.isEmpty()) {
            demonstrated.add("Demonstrated technical aptitude in " + String.join(", ", combinedSkills.subList(0, Math.min(4, combinedSkills.size()))));
        }

        List<String> missing = new ArrayList<>();
        if (bio.isBlank()) {
            missing.add("Professional Bio: Missing immediate candidate summary");
        }
        if (totalStars == 0) {
            missing.add("Social Proof: No pinned or starred flagship projects showcased");
        }
        missing.add("Live Demos / Deployment links: Most projects lack accessible demo links");

        String verdict = (publicRepos >= 3 && !bio.isBlank())
                ? "Above Average: Profile conveys genuine technical work. Polish repository descriptions to maximize interview callbacks."
                : (publicRepos > 0)
                ? "Standard: Code is visible, but profile lacks presentation polish. Recruiters spend extra time searching for key skills."
                : "Needs Attention: Critical signals are absent. Recruiters cannot verify practical implementation skills.";

        String advice = "Recruiters evaluate candidate profiles using 3 criteria: (1) Can this candidate write clean code? (2) Do they understand modern tooling? (3) Can they document their work? Address the missing signals above to rank in the top 10% of applicants.";

        return RecruiterViewDto.builder()
                .immediateImpressions(immediate)
                .demonstratedSkills(demonstrated)
                .missingSignals(missing)
                .evaluationVerdict(verdict)
                .actionableAdvice(advice)
                .build();
    }

    private static class RepoInfo {
        String name;
        String htmlUrl;
        String description;
        String language;
        int stars;
        int forks;
        String homepage;
        List<String> topics;

        RepoInfo(String name, String htmlUrl, String description, String language, int stars, int forks, String homepage, List<String> topics) {
            this.name = name != null ? name : "";
            this.htmlUrl = htmlUrl != null ? htmlUrl : "";
            this.description = description != null ? description : "";
            this.language = language != null ? language : "";
            this.stars = stars;
            this.forks = forks;
            this.homepage = homepage != null ? homepage.trim() : "";
            this.topics = topics != null ? topics : Collections.emptyList();
        }
    }
}
