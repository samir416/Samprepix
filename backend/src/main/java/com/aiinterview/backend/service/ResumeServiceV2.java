package com.aiinterview.backend.service;

import com.aiinterview.backend.model.ResumeResponse;
import com.aiinterview.backend.dto.ai.AIResponse;
import com.aiinterview.backend.entity.ResumeAnalysis;
import java.time.LocalDateTime;
import com.aiinterview.backend.service.analyzer.AIResumeAnalyzerService;
import com.aiinterview.backend.service.history.ResumeHistoryService;
import com.aiinterview.backend.service.parser.DocxParserService;
import com.aiinterview.backend.service.parser.PdfParserService;
import com.aiinterview.backend.service.validation.ResumeValidationService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ResumeServiceV2 {

    private final ResumeValidationService validationService;
    private final PdfParserService pdfParserService;
    private final DocxParserService docxParserService;
    private final AIResumeAnalyzerService aiResumeAnalyzerService;
    private final ResumeHistoryService resumeHistoryService;

    public ResumeServiceV2(
            ResumeValidationService validationService,
            PdfParserService pdfParserService,
            DocxParserService docxParserService,
            AIResumeAnalyzerService aiResumeAnalyzerService,
            ResumeHistoryService resumeHistoryService
    ) {
        this.validationService = validationService;
        this.pdfParserService = pdfParserService;
        this.docxParserService = docxParserService;
        this.aiResumeAnalyzerService = aiResumeAnalyzerService;
        this.resumeHistoryService = resumeHistoryService;
    }

   
    

    public String extractTextFromPdf(MultipartFile file) {
    return extractResumeText(file);
}

public String extractResumeText(MultipartFile file) {

    validationService.validate(file);

    String fileName = file.getOriginalFilename();

    if (fileName == null) {
        throw new IllegalArgumentException("Invalid resume file.");
    }

    String extension = fileName.substring(fileName.lastIndexOf('.') + 1)
            .toLowerCase();

    return switch (extension) {
        case "pdf" -> pdfParserService.extractText(file);
        case "docx" -> docxParserService.extractText(file);
        default -> throw new IllegalArgumentException("Unsupported resume format.");
    };
}

    public List<String> detectSkills(String resumeText) {
        List<String> detectedSkills = new java.util.ArrayList<>();
        String text = resumeText == null ? "" : resumeText.toLowerCase();

        String[] skills = {"java","sql","html","css","javascript","react","spring boot","mysql","hibernate","git","rest api","c","c++"};

        for (String skill : skills) {
            if (text.contains(skill)) {
                detectedSkills.add(skill);
            }
        }

        return detectedSkills;
    }

    public String detectRole(String resumeText) {
        String text = resumeText == null ? "" : resumeText.toLowerCase();

        boolean frontend = text.contains("html") || text.contains("css") || text.contains("javascript") || text.contains("react");
        boolean backend = text.contains("java") || text.contains("spring boot") || text.contains("hibernate") || text.contains("mysql") || text.contains("sql");

        if (frontend && backend) return "FULL_STACK";
        if (backend) return "JAVA_BACKEND";
        if (frontend) return "FRONTEND";
        return "GENERAL";
    }

    public List<String> getRequiredSkills(String role) {
        switch (role) {
            case "JAVA_BACKEND":
                return List.of("java","spring boot","sql","mysql","hibernate","git","rest api");
            case "FRONTEND":
                return List.of("html","css","javascript","react","git");
            case "FULL_STACK":
                return List.of("java","spring boot","sql","mysql","html","css","javascript","react","git");
            default:
                return List.of("java","sql","html");
        }
    }

    public int calculateScore(List<String> detectedSkills, String role) {
        List<String> requiredSkills = getRequiredSkills(role);
        int matched = 0;
        for (String s : requiredSkills) if (detectedSkills.contains(s)) matched++;
        return (matched * 100) / Math.max(1, requiredSkills.size());
    }

    public List<String> findMissingSkills(List<String> detectedSkills, String role) {
        List<String> required = getRequiredSkills(role);
        List<String> missing = new java.util.ArrayList<>();
        for (String s : required) if (!detectedSkills.contains(s)) missing.add(s);
        return missing;
    }

    public List<String> generateSuggestions(List<String> missingSkills, String role) {
        List<String> suggestions = new java.util.ArrayList<>();
        for (String skill : missingSkills) suggestions.add("Add or improve " + skill + " skills");
        switch (role) {
            case "JAVA_BACKEND":
                suggestions.add("Build Spring Boot REST API projects");
                suggestions.add("Practice database design and SQL");
                break;
            case "FRONTEND":
                suggestions.add("Build responsive React projects");
                suggestions.add("Improve JavaScript fundamentals");
                break;
            case "FULL_STACK":
                suggestions.add("Build end-to-end full stack projects");
                suggestions.add("Deploy projects on cloud platforms");
                break;
        }
        return suggestions;
    }

 public ResumeResponse analyzeResumeFile(MultipartFile file, String email) throws Exception {

    String text = extractResumeText(file);

    AIResponse aiResponse = aiResumeAnalyzerService.analyzeResume(text);

    ResumeResponse response = buildResponse(aiResponse, text);

    saveAnalysis(email, response);

    return response;
}
        

    public List<ResumeAnalysis> getHistory(String email) {
        return resumeHistoryService.getHistory(email);
    }

    public ResumeAnalysis getLatestAnalysis(String email) {
        return resumeHistoryService.getLatestAnalysis(email);
    }

    private ResumeResponse buildResponse(AIResponse aiResponse, String resumeText) {

    ResumeResponse response = new ResumeResponse();

    response.setValidResume(true);
    response.setRole(aiResponse.getRole());
    response.setScore(aiResponse.getAtsScore() == null ? 0 : aiResponse.getAtsScore());
    response.setSkills(aiResponse.getDetectedSkills());
    response.setMissingSkills(aiResponse.getMissingSkills());
    response.setSuggestions(aiResponse.getSuggestions());

    response.setGithubFound(hasGithub(resumeText));
    response.setLinkedinFound(hasLinkedIn(resumeText));
    response.setProjectFound(hasProjects(resumeText));
    response.setEducationFound(hasEducation(resumeText));
    response.setExperienceFound(hasExperience(resumeText));

    return response;
}

private void saveAnalysis(String email, ResumeResponse response) {

    ResumeAnalysis analysis = new ResumeAnalysis();

    analysis.setUserEmail(email);
    analysis.setScore(response.getScore());
    analysis.setSkills(response.getSkills() == null ? "" : String.join(", ", response.getSkills()));
    analysis.setMissingSkills(response.getMissingSkills() == null ? "" : String.join(", ", response.getMissingSkills()));
    analysis.setSuggestions(response.getSuggestions() == null ? "" : String.join(", ", response.getSuggestions()));
    analysis.setAnalyzedAt(LocalDateTime.now().toString());

    resumeHistoryService.save(analysis);
}


    private boolean hasGithub(String text) {
    return text != null && text.toLowerCase().contains("github");
}

private boolean hasLinkedIn(String text) {
    return text != null && text.toLowerCase().contains("linkedin");
}

private boolean hasProjects(String text) {
    if (text == null) {
        return false;
    }

    text = text.toLowerCase();

    return text.contains("project")
            || text.contains("projects")
            || text.contains("developed")
            || text.contains("built");
}

private boolean hasEducation(String text) {
    if (text == null) {
        return false;
    }

    text = text.toLowerCase();

    return text.contains("bca")
            || text.contains("bsc")
            || text.contains("mca")
            || text.contains("btech")
            || text.contains("degree")
            || text.contains("college")
            || text.contains("university");
}

private boolean hasExperience(String text) {
    if (text == null) {
        return false;
    }

    text = text.toLowerCase();

    return text.contains("experience")
            || text.contains("internship")
            || text.contains("intern")
            || text.contains("worked");
}

}