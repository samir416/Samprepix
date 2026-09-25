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

        // 3. Fetch Profile README (if any)
        String currentReadme = fetchProfileReadme(username);

        // 4. Gather Samprepix context
        UserProfile profile = userProfileRepository.findByUser(user).orElse(null);
        String userEmail = user.getEmail() != null ? user.getEmail().trim() : "";
        List<ResumeAnalysis> resumeAnalyses = userEmail.isBlank() ? List.of() : resumeAnalysisRepository.findByUserEmail(userEmail);
        ResumeAnalysis latestResume = (resumeAnalyses != null && !resumeAnalyses.isEmpty()) ? resumeAnalyses.get(0) : null;

        // 5. Aggregate Technical Signals (Priority 1: GitHub, Priority 2: Samprepix)
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

                totalStars += stars;
                totalForks += forks;

                if (!desc.isBlank()) {
                    reposWithDescription++;
                }

                if (!lang.isBlank()) {
                    languageCounts.put(lang, languageCounts.getOrDefault(lang, 0) + 1);
                }

                repoList.add(new RepoInfo(name, htmlUrl, desc, lang, stars, forks));
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

        if (profile != null && profile.getSkills() != null) {
            for (String s : profile.getSkills()) {
                if (!combinedSkills.contains(s) && combinedSkills.size() < 12) {
                    combinedSkills.add(s);
                }
            }
        }

        String targetRole = (profile != null && profile.getTargetRole() != null && !profile.getTargetRole().isBlank())
                ? profile.getTargetRole()
                : "Software Engineer";

        // 6. Deterministic Category Scoring (0 to 100)
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

        // Category 1: Profile Completeness (Max 20)
        int scoreProfile = 0;
        if (!ghName.isBlank()) scoreProfile += 4;
        if (!ghAvatar.isBlank()) scoreProfile += 4;
        if (!ghBio.isBlank()) {
            scoreProfile += 6;
            if (ghBio.length() < 20) {
                deductions.add("Brief bio: Your GitHub bio is very short. Expand it with your target role and core stack.");
            }
        } else {
            deductions.add("Missing GitHub bio: Recruiters scan your bio within 5 seconds of opening your profile.");
            improvements.add("Add a concise professional bio highlighting: '" + targetRole + " specializing in " + (combinedSkills.isEmpty() ? "modern full-stack development" : String.join(", ", combinedSkills.subList(0, Math.min(3, combinedSkills.size())))) + ".'");
        }
        if (!ghLocation.isBlank()) scoreProfile += 3;
        if (!ghBlog.isBlank() || !ghCompany.isBlank()) scoreProfile += 3;
        scoreProfile = Math.min(20, Math.max(4, scoreProfile));

        // Category 2: Repository Quality & Diversity (Max 25)
        int scoreProjects = 0;
        if (publicRepos > 0) scoreProjects += 6;
        if (publicRepos >= 3) scoreProjects += 6;
        if (reposWithDescription >= 2) scoreProjects += 6;
        if (totalStars > 0 || totalForks > 0) scoreProjects += 4;
        if (topLanguages.size() >= 2) scoreProjects += 3;

        if (publicRepos == 0) {
            deductions.add("Zero public repositories: Recruiters cannot inspect your code or problem-solving ability.");
            improvements.add("Publish at least 2 complete, well-structured projects showcasing your " + targetRole + " skills.");
        } else if (reposWithDescription < Math.min(3, repoList.size())) {
            deductions.add("Missing repository descriptions: " + (repoList.size() - reposWithDescription) + " of your public repositories have no summary.");
            improvements.add("Add 1-2 sentence descriptions to all public repositories specifying the project purpose and tech stack.");
        }
        scoreProjects = Math.min(25, Math.max(3, scoreProjects));

        // Category 3: Profile README (Max 20)
        int scoreReadme = 0;
        boolean hasProfileReadme = currentReadme != null && !currentReadme.isBlank();
        if (hasProfileReadme) {
            scoreReadme += 10;
            if (currentReadme.length() > 200) scoreReadme += 5;
            if (currentReadme.toLowerCase().contains("skill") || currentReadme.toLowerCase().contains("tech")) scoreReadme += 3;
            if (currentReadme.toLowerCase().contains("project") || currentReadme.toLowerCase().contains("connect")) scoreReadme += 2;
        } else {
            deductions.add("No special profile README found (at " + username + "/" + username + ").");
            improvements.add("Create a repository named exactly '" + username + "' to enable the special GitHub Profile README banner.");
        }
        scoreReadme = Math.min(20, scoreReadme);

        // Category 4: Documentation & Setup (Max 15)
        int scoreDocumentation = 0;
        if (reposWithDescription > 0) scoreDocumentation += 5;
        if (hasProfileReadme) scoreDocumentation += 5;
        if (publicRepos >= 2) scoreDocumentation += 5;
        if (reposWithDescription < repoList.size() / 2 && !repoList.isEmpty()) {
            deductions.add("Low documentation coverage: Most repositories lack comprehensive setup instructions.");
            improvements.add("Include prerequisites, installation commands, environment variable guides, and screenshots in project READMEs.");
        }
        scoreDocumentation = Math.min(15, Math.max(2, scoreDocumentation));

        // Category 5: Professional Presentation & Recruiter Readability (Max 20)
        int scorePresentation = 0;
        if (!combinedSkills.isEmpty()) scorePresentation += 6;
        if (!ghName.isBlank() && !ghAvatar.isBlank()) scorePresentation += 6;
        if (!ghBio.isBlank() && hasProfileReadme) scorePresentation += 5;
        if (publicRepos >= 3) scorePresentation += 3;
        scorePresentation = Math.min(20, Math.max(3, scorePresentation));

        int overallScore = scoreProfile + scoreProjects + scoreReadme + scoreDocumentation + scorePresentation;
        overallScore = Math.min(100, Math.max(10, overallScore));

        // Category DTOs
        List<CategoryScoreDto> categoryScores = List.of(
                new CategoryScoreDto("Profile Completeness", scoreProfile, 20, scoreProfile >= 16 ? "Strong profile identity & credentials" : "Profile details need attention"),
                new CategoryScoreDto("Repository Quality & Diversity", scoreProjects, 25, scoreProjects >= 20 ? "Solid portfolio of active code repositories" : "Expand repository variety & descriptions"),
                new CategoryScoreDto("Profile README", scoreReadme, 20, hasProfileReadme ? "Personal profile README is configured" : "Special profile README is missing"),
                new CategoryScoreDto("Documentation & Setup", scoreDocumentation, 15, scoreDocumentation >= 12 ? "Good technical clarity and descriptions" : "Add setup steps, tech stack & demo links"),
                new CategoryScoreDto("Recruiter Readability", scorePresentation, 20, scorePresentation >= 16 ? "Fast, credible technical impression for recruiters" : "Optimize for 30-second recruiter scans")
        );

        // 7. Profile README Generator & Suggestions (Keep, Improve, Remove, Add)
        String recommendedReadme = generateRecommendedReadme(username, ghName, targetRole, combinedSkills, repoList, ghBlog, profile, currentReadme);
        Map<String, List<String>> readmeSuggestions = generateReadmeSuggestions(hasProfileReadme, currentReadme, combinedSkills, targetRole);

        // 8. Repository Specific Recommendations
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
            recs.add("Include a clean README with Architecture Diagram, Setup steps, and Live Demo link.");
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

        // 9. Recruiter View
        RecruiterViewDto recruiterView = buildRecruiterView(username, ghBio, publicRepos, topLanguages, combinedSkills, targetRole, hasProfileReadme, totalStars);

        // 10. Persist analysis result
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
                    .currentReadme(currentReadme)
                    .recommendedReadme(recommendedReadme)
                    .readmeDiffJson(objectMapper.writeValueAsString(readmeSuggestions))
                    .repoAnalysesJson(objectMapper.writeValueAsString(repoAnalyses))
                    .recruiterView(objectMapper.writeValueAsString(recruiterView))
                    .analyzedAt(LocalDateTime.now())
                    .build();
            analysisResultRepository.save(result);
        } catch (Exception e) {
            log.error("Failed to persist GitHub analysis result: {}", e.getMessage());
        }

        // 11. Build Response (applying free preview gating if not premium)
        return buildResponse(
                username, ghName, ghAvatar, profileUrl, ghBio, ghLocation, ghCompany, ghBlog,
                publicRepos, followers, following, overallScore, categoryScores, deductions, improvements,
                topLanguages, currentReadme, recommendedReadme, readmeSuggestions, repoAnalyses,
                recruiterView, isPremium, effectivePlan
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
                Map<String, List<String>> readmeSuggestions = objectMapper.readValue(result.getReadmeDiffJson(), new TypeReference<>() {});
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
                        result.getCurrentReadme(),
                        result.getRecommendedReadme(),
                        readmeSuggestions,
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
            List<Map<String, Object>> topLanguages, String currentReadme, String recommendedReadme,
            Map<String, List<String>> readmeSuggestions, List<RepoAnalysisDto> repoAnalyses,
            RecruiterViewDto recruiterView, boolean isPremium, String effectivePlan
    ) {
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));

        if (!isPremium) {
            // Free preview gating: provide overall score & category scores, but mask deep recommendations & README generator
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
                    .deductions(deductions.stream().limit(2).toList())
                    .improvements(improvements.stream().limit(2).toList())
                    .topLanguages(topLanguages)
                    .currentReadme(currentReadme != null ? currentReadme.substring(0, Math.min(150, currentReadme.length())) + "\n\n... (Upgrade to Pro/Elite to inspect full analysis)" : null)
                    .recommendedReadme("### [PREMIUM FEATURE: Upgrade to Pro or Elite to unlock your tailored Profile README]")
                    .readmeSuggestions(Map.of(
                            "keep", List.of("Active repository commits"),
                            "improve", List.of("Profile README presentation (Locked in Starter)"),
                            "add", List.of("Tailored technical skills matrix (Locked in Starter)"),
                            "remove", List.of("Unused/forked repositories")
                    ))
                    .repoAnalyses(repoAnalyses.stream().limit(1).toList())
                    .recruiterView(RecruiterViewDto.builder()
                            .immediateImpressions("Recruiter view summary is locked. Upgrade to Pro or Elite for full recruiter evaluation metrics.")
                            .demonstratedSkills(topLanguages.stream().map(m -> String.valueOf(m.get("name"))).limit(3).toList())
                            .missingSignals(List.of("Detailed technical signals hidden in free tier"))
                            .evaluationVerdict("Preview Mode: Basic score visible.")
                            .actionableAdvice("Upgrade to unlock full analysis, copyable README, and repository audit.")
                            .build())
                    .premium(false)
                    .effectivePlan(effectivePlan)
                    .analyzedAt(formattedDate)
                    .build();
        }

        // Full Premium Response
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
                .currentReadme(currentReadme)
                .recommendedReadme(recommendedReadme)
                .readmeSuggestions(readmeSuggestions)
                .repoAnalyses(repoAnalyses)
                .recruiterView(recruiterView)
                .premium(true)
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

    private String fetchProfileReadme(String username) {
        try {
            // First try raw.githubusercontent.com for main branch
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://raw.githubusercontent.com/" + username + "/" + username + "/main/README.md"))
                    .header("User-Agent", "Samprepix-Platform")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && !response.body().isBlank()) {
                return response.body();
            }

            // Fallback to master branch
            request = HttpRequest.newBuilder()
                    .uri(URI.create("https://raw.githubusercontent.com/" + username + "/" + username + "/master/README.md"))
                    .header("User-Agent", "Samprepix-Platform")
                    .GET()
                    .build();

            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && !response.body().isBlank()) {
                return response.body();
            }
        } catch (Exception e) {
            log.debug("No profile README found for {}: {}", username, e.getMessage());
        }
        return null;
    }

    private String generateRecommendedReadme(
            String username, String name, String targetRole, List<String> skills,
            List<RepoInfo> repos, String blog, UserProfile profile, String currentReadme
    ) {
        StringBuilder sb = new StringBuilder();
        String displayName = (name != null && !name.isBlank()) ? name : username;

        sb.append("# Hi, I'm ").append(displayName).append(" 👋\n\n");
        sb.append("### 🚀 ").append(targetRole).append("\n\n");

        if (currentReadme != null && currentReadme.length() > 50 && !currentReadme.contains("### [PREMIUM FEATURE")) {
            String[] lines = currentReadme.split("\n");
            StringBuilder introBuilder = new StringBuilder();
            int capturedLines = 0;
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.startsWith("#") || trimmed.startsWith("---") || trimmed.startsWith("![")) {
                    continue;
                }
                if (!trimmed.isBlank() && capturedLines < 3) {
                    introBuilder.append(trimmed).append(" ");
                    capturedLines++;
                }
            }
            String extractedIntro = introBuilder.toString().trim();
            if (!extractedIntro.isBlank()) {
                sb.append(extractedIntro).append("\n\n");
            } else {
                sb.append("Passionate software engineer focused on building robust, scalable applications and solving challenging computational problems. ")
                  .append("Currently refining algorithms, backend architecture, and technical interview readiness on Samprepix.\n\n");
            }
        } else {
            sb.append("Passionate software engineer focused on building robust, scalable applications and solving challenging computational problems. ")
              .append("Currently refining algorithms, backend architecture, and technical interview readiness on Samprepix.\n\n");
        }

        sb.append("---\n\n");
        sb.append("### 🛠️ Technical Skills\n\n");

        if (!skills.isEmpty()) {
            sb.append("- **Core Stack:** ").append(String.join(", ", skills)).append("\n");
            sb.append("- **Focus Areas:** Clean Architecture, Data Structures & Algorithms, RESTful APIs, Cloud Deployment\n\n");
        } else {
            sb.append("- **Focus Areas:** Clean Architecture, Data Structures & Algorithms, Problem Solving\n\n");
        }

        if (!repos.isEmpty()) {
            sb.append("---\n\n");
            sb.append("### 💻 Featured Projects\n\n");
            int added = 0;
            for (RepoInfo repo : repos) {
                if (added >= 3) break;
                sb.append("- **[").append(repo.name).append("](").append(repo.htmlUrl).append(")** - ")
                  .append(repo.description.isBlank() ? "High-performance software application engineered with " + (repo.language.isBlank() ? "modern tooling" : repo.language) + "." : repo.description)
                  .append("\n");
                added++;
            }
            sb.append("\n");
        }

        sb.append("---\n\n");
        sb.append("### 📈 Continuous Learning & Goals\n\n");
        sb.append("- 🎯 Actively solving Data Structures & Algorithms challenges daily.\n");
        sb.append("- 🔭 Exploring microservices, distributed caching, and scalable system design.\n");
        sb.append("- 💬 Open to discussions about software engineering and technical collaborations.\n\n");

        sb.append("---\n\n");
        sb.append("### 📫 Let's Connect\n\n");
        sb.append("- **GitHub:** [github.com/").append(username).append("](https://github.com/").append(username).append(")\n");
        if (profile != null && profile.getLinkedinUrl() != null && !profile.getLinkedinUrl().isBlank()) {
            sb.append("- **LinkedIn:** [").append(profile.getLinkedinUrl()).append("](").append(profile.getLinkedinUrl()).append(")\n");
        }
        if (blog != null && !blog.isBlank()) {
            sb.append("- **Portfolio / Website:** [").append(blog).append("](").append(blog.startsWith("http") ? blog : "https://" + blog).append(")\n");
        }

        return sb.toString();
    }

    public GithubAnalysisResponse applyReadmeToGithub(User user, String targetUsername, String readmeContent) {
        if (user == null) {
            throw new IllegalArgumentException("User must be authenticated.");
        }
        if (targetUsername == null || targetUsername.isBlank()) {
            throw new IllegalArgumentException("Target username is required.");
        }
        if (readmeContent == null || readmeContent.isBlank()) {
            throw new IllegalArgumentException("README content cannot be empty.");
        }

        GitHubConnection connection = gitHubConnectionRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Your GitHub account is not connected. Please connect your GitHub account in Profile Settings with write permission to apply changes directly."));

        String token = connection.getAccessToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Missing authorized GitHub access token. Please reconnect your GitHub account.");
        }

        String authUsername = fetchAuthenticatedGitHubUsername(token);
        if (authUsername == null || !authUsername.equalsIgnoreCase(targetUsername.trim())) {
            throw new IllegalArgumentException("Connected GitHub account (@" + (authUsername != null ? authUsername : "unknown")
                    + ") does not match the profile @" + targetUsername + ". You can only apply updates to your own profile README.");
        }

        String repoName = authUsername + "/" + authUsername;
        String existingSha = getFileSha(token, authUsername, authUsername, "README.md");

        boolean success = writeReadmeFile(token, authUsername, authUsername, "README.md", readmeContent, existingSha);
        if (!success) {
            throw new IllegalStateException("Failed to commit README to GitHub repository " + repoName + ". Please ensure your GitHub token has write access.");
        }

        return analyzeProfile(user, "https://github.com/" + authUsername);
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

    private String getFileSha(String token, String owner, String repo, String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Samprepix-Platform")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode node = objectMapper.readTree(response.body());
                return node.path("sha").asText(null);
            }
        } catch (Exception e) {
            log.debug("No existing file found for {}/{}/{}: {}", owner, repo, path, e.getMessage());
        }
        return null;
    }

    private boolean writeReadmeFile(String token, String owner, String repo, String path, String content, String sha) {
        try {
            HttpRequest repoCheck = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repo))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Samprepix-Platform")
                    .GET()
                    .build();
            HttpResponse<String> repoResp = httpClient.send(repoCheck, HttpResponse.BodyHandlers.ofString());
            if (repoResp.statusCode() == 404) {
                Map<String, Object> createPayload = Map.of(
                        "name", repo,
                        "description", "Personal GitHub Profile README configured via Samprepix",
                        "auto_init", true,
                        "private", false
                );
                HttpRequest createReq = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/user/repos"))
                        .header("Authorization", "Bearer " + token)
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("User-Agent", "Samprepix-Platform")
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(createPayload)))
                        .build();
                HttpResponse<String> createResp = httpClient.send(createReq, HttpResponse.BodyHandlers.ofString());
                if (createResp.statusCode() != 201 && createResp.statusCode() != 200) {
                    log.error("Failed to create special profile repo {}/{}: status {}", owner, repo, createResp.statusCode());
                    return false;
                }
                Thread.sleep(1200);
                sha = getFileSha(token, owner, repo, path);
            }

            Map<String, Object> putPayload = new LinkedHashMap<>();
            putPayload.put("message", "docs(profile): update Profile README with verified technical skills via Samprepix");
            putPayload.put("content", Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8)));
            if (sha != null && !sha.isBlank()) {
                putPayload.put("sha", sha);
            }

            HttpRequest putReq = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repo + "/contents/" + path))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "Samprepix-Platform")
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(putPayload)))
                    .build();

            HttpResponse<String> putResp = httpClient.send(putReq, HttpResponse.BodyHandlers.ofString());
            return putResp.statusCode() == 200 || putResp.statusCode() == 201;
        } catch (Exception e) {
            log.error("Exception writing README to GitHub: {}", e.getMessage());
            return false;
        }
    }

    private Map<String, List<String>> generateReadmeSuggestions(boolean hasReadme, String currentReadme, List<String> skills, String targetRole) {
        Map<String, List<String>> suggestions = new LinkedHashMap<>();

        if (!hasReadme) {
            suggestions.put("Keep", List.of("Your active GitHub contribution history and genuine commit graph."));
            suggestions.put("Improve", List.of("Profile discoverability: Currently visitors only see your pinned repo cards without context."));
            suggestions.put("Remove", List.of("Generic commit messages without ticket or feature descriptions."));
            suggestions.put("Add", List.of(
                    "Special Profile README banner with your target role: '" + targetRole + "'",
                    "Clear skills grid grouped by Languages, Frameworks, and Tools",
                    "Links to 2-3 flagship projects with one-line value propositions",
                    "LinkedIn and verified contact details for recruiters"
            ));
        } else {
            suggestions.put("Keep", List.of("Existing introduction and personal tone in your current README."));
            suggestions.put("Improve", List.of(
                    "Format skill badges cleanly: Avoid cluttering with 40+ generic logos.",
                    "Highlight measurable project outcomes (e.g. 'Reduced latency by 40%', 'Processed 10k items')."
            ));
            suggestions.put("Remove", List.of(
                    "Static non-working external widgets or outdated statistics badges.",
                    "Unmaintained or toy tutorial repositories without documentation."
            ));
            suggestions.put("Add", List.of(
                    "Clear target domain specification (" + targetRole + ")",
                    "Architecture summaries and live demo links for featured repositories"
            ));
        }

        return suggestions;
    }

    private RecruiterViewDto buildRecruiterView(
            String username, String bio, int publicRepos, List<Map<String, Object>> topLangs,
            List<String> combinedSkills, String targetRole, boolean hasProfileReadme, int totalStars
    ) {
        String immediate = "A tech recruiter reviewing this profile in 30 seconds sees "
                + (publicRepos > 0 ? publicRepos + " public repositories" : "no public code")
                + " with primary focus on "
                + (topLangs.isEmpty() ? "unspecified technologies" : topLangs.get(0).get("name"))
                + ". "
                + (hasProfileReadme ? "The profile has a custom README banner establishing clear identity." : "A custom profile README is missing, requiring the recruiter to guess your career target.");

        List<String> demonstrated = new ArrayList<>();
        topLangs.forEach(l -> demonstrated.add("Active code in " + l.get("name") + " (" + l.get("percentage") + "% of repositories)"));
        if (!combinedSkills.isEmpty()) {
            demonstrated.add("Demonstrated technical aptitude in " + String.join(", ", combinedSkills.subList(0, Math.min(4, combinedSkills.size()))));
        }

        List<String> missing = new ArrayList<>();
        if (bio.isBlank()) {
            missing.add("Professional Bio: Missing immediate candidate summary");
        }
        if (!hasProfileReadme) {
            missing.add("Profile README: No centralized skill and project overview");
        }
        if (totalStars == 0) {
            missing.add("Social Proof: No pinned or starred flagship projects showcased");
        }
        missing.add("Live Demos / Deployment links: Most projects lack accessible demo links");

        String verdict = (publicRepos >= 3 && hasProfileReadme)
                ? "Above Average: Profile conveys genuine technical work. Polish repository READMEs to maximize interview callbacks."
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

        RepoInfo(String name, String htmlUrl, String description, String language, int stars, int forks) {
            this.name = name;
            this.htmlUrl = htmlUrl;
            this.description = description;
            this.language = language;
            this.stars = stars;
            this.forks = forks;
        }
    }
}
