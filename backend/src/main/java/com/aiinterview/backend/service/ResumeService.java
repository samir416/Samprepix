package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.ResumeAnalysis;
import com.aiinterview.backend.model.ResumeResponse;
import com.aiinterview.backend.repository.ResumeAnalysisRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeService {

        private final ResumeAnalysisRepository resumeAnalysisRepository;

        public ResumeService(
                        ResumeAnalysisRepository resumeAnalysisRepository) {

                this.resumeAnalysisRepository = resumeAnalysisRepository;
        }

        public ResumeResponse analyzeResume() {

                ResumeResponse response = new ResumeResponse();

                response.setRole(
                                "FULL_STACK");

                response.setScore(
                                78);

                response.setSkills(
                                List.of(
                                                "Java",
                                                "SQL",
                                                "HTML",
                                                "CSS"));

                response.setMissingSkills(
                                List.of(
                                                "Spring Boot",
                                                "React"));

                response.setSuggestions(
                                List.of(
                                                "Add more projects",
                                                "Improve resume summary",
                                                "Add GitHub profile"));

                return response;
        }

        public String extractTextFromPdf(
                        MultipartFile file)
                        throws IOException {

                PDDocument document = Loader.loadPDF(
                                file.getBytes());

                PDFTextStripper stripper = new PDFTextStripper();

                String text = stripper.getText(
                                document);

                document.close();

                return text;
        }

        public List<String> detectSkills(
                        String resumeText) {

                List<String> detectedSkills = new ArrayList<>();

                String text = resumeText.toLowerCase();

                String[] skills = {

                                "java",
                                "sql",
                                "html",
                                "css",
                                "javascript",
                                "react",
                                "spring boot",
                                "mysql",
                                "hibernate",
                                "git",
                                "rest api",
                                "c",
                                "c++"
                };

                for (String skill : skills) {

                        if (text.contains(skill)) {

                                detectedSkills.add(
                                                skill);
                        }
                }

                return detectedSkills;
        }

        public String detectRole(
                        String resumeText) {

                String text = resumeText.toLowerCase();

                boolean frontend =

                                text.contains("html")
                                                || text.contains("css")
                                                || text.contains("javascript")
                                                || text.contains("react");

                boolean backend =

                                text.contains("java")
                                                || text.contains("spring boot")
                                                || text.contains("hibernate")
                                                || text.contains("mysql")
                                                || text.contains("sql");

                if (frontend && backend) {

                        return "FULL_STACK";
                }

                if (backend) {

                        return "JAVA_BACKEND";
                }

                if (frontend) {

                        return "FRONTEND";
                }

                return "GENERAL";
        }

        public List<String> getRequiredSkills(
                        String role) {

                switch (role) {

                        case "JAVA_BACKEND":

                                return List.of(
                                                "java",
                                                "spring boot",
                                                "sql",
                                                "mysql",
                                                "hibernate",
                                                "git",
                                                "rest api");

                        case "FRONTEND":

                                return List.of(
                                                "html",
                                                "css",
                                                "javascript",
                                                "react",
                                                "git");

                        case "FULL_STACK":

                                return List.of(
                                                "java",
                                                "spring boot",
                                                "sql",
                                                "mysql",
                                                "html",
                                                "css",
                                                "javascript",
                                                "react",
                                                "git");

                        default:

                                return List.of(
                                                "java",
                                                "sql",
                                                "html");
                }
        }

        public int calculateScore(
                        List<String> detectedSkills,
                        String role) {

                List<String> requiredSkills = getRequiredSkills(
                                role);

                int matchedSkills = 0;

                for (String skill : requiredSkills) {

                        if (detectedSkills.contains(
                                        skill)) {

                                matchedSkills++;
                        }
                }

                return (matchedSkills * 100)
                                / requiredSkills.size();
        }

        public List<String> findMissingSkills(
                        List<String> detectedSkills,
                        String role) {

                List<String> requiredSkills = getRequiredSkills(
                                role);

                List<String> missingSkills = new ArrayList<>();

                for (String skill : requiredSkills) {

                        if (!detectedSkills.contains(
                                        skill)) {

                                missingSkills.add(
                                                skill);
                        }
                }

                return missingSkills;
        }

        public List<String> generateSuggestions(
                        List<String> missingSkills,
                        String role) {

                List<String> suggestions = new ArrayList<>();

                for (String skill : missingSkills) {

                        suggestions.add(
                                        "Add or improve "
                                                        + skill
                                                        + " skills");
                }

                switch (role) {

                        case "JAVA_BACKEND" -> {

                                suggestions.add(
                                                "Build Spring Boot REST API projects");

                                suggestions.add(
                                                "Practice database design and SQL");
                        }

                        case "FRONTEND" -> {

                                suggestions.add(
                                                "Build responsive React projects");

                                suggestions.add(
                                                "Improve JavaScript fundamentals");
                        }

                        case "FULL_STACK" -> {

                                suggestions.add(
                                                "Build end-to-end full stack projects");

                                suggestions.add(
                                                "Deploy projects on cloud platforms");
                        }
                }

                return suggestions;
        }

        public ResumeResponse analyzeResumeFile(
                        MultipartFile file,
                        String email)
                        throws Exception {

                String text = extractTextFromPdf(
                                file);

                List<String> skills = detectSkills(
                                text);

                if (!isITResume(skills)) {

                        ResumeResponse response = new ResumeResponse();


                        response.setValidResume( false);

                        response.setRejectionReason( "NON_IT_RESUME");

                        response.setScore(0);

                        return response;
                }

                String role = detectRole(
                                text);

                int score = calculateScore(
                                skills,
                                role);

                List<String> missingSkills = findMissingSkills(
                                skills,
                                role);

                if (hasProjects(text)) {

                        score += 10;
                }

                if (hasEducation(text)) {

                        score += 5;
                }

                if (hasExperience(text)) {

                        score += 5;
                }

                if (hasGithub(text)) {

                        score += 5;
                }

                if (hasLinkedIn(text)) {

                        score += 5;
                }

                if (score > 100) {

                        score = 100;
                }

                ResumeResponse response = new ResumeResponse();

                response.setValidResume( true);

                response.setRole(
                                role);

                response.setScore(
                                score);

                response.setSkills(
                                skills);

                response.setMissingSkills(
                                missingSkills);

                response.setSuggestions(
                                generateSuggestions(
                                                missingSkills,
                                                role));

                response.setGithubFound(
                                hasGithub(text));

                response.setLinkedinFound(
                                hasLinkedIn(text));

                response.setProjectFound(
                                hasProjects(text));

                response.setEducationFound(
                                hasEducation(text));

                response.setExperienceFound(
                                hasExperience(text));

                ResumeAnalysis analysis = new ResumeAnalysis();

                analysis.setUserEmail(
                                email);

                analysis.setScore(
                                score);

                analysis.setSkills(
                                String.join(
                                                ", ",
                                                skills));

                analysis.setMissingSkills(
                                String.join(
                                                ", ",
                                                missingSkills));

                analysis.setSuggestions(
                                String.join(
                                                ", ",
                                                response.getSuggestions()));

                analysis.setAnalyzedAt(
                                LocalDateTime.now()
                                                .toString());

                resumeAnalysisRepository
                                .save(
                                                analysis);

                return response;
        }

        public boolean hasGithub(
                        String resumeText) {

                return resumeText
                                .toLowerCase()
                                .contains(
                                                "github");
        }

        public boolean hasLinkedIn(
                        String resumeText) {

                return resumeText
                                .toLowerCase()
                                .contains(
                                                "linkedin");
        }

        public boolean hasProjects(
                        String resumeText) {

                String text = resumeText.toLowerCase();

                return text.contains(
                                "project")
                                || text.contains(
                                                "projects")
                                || text.contains(
                                                "developed")
                                || text.contains(
                                                "built");
        }

        public boolean hasEducation(
                        String resumeText) {

                String text = resumeText.toLowerCase();

                return text.contains(
                                "bca")
                                || text.contains(
                                                "bsc")
                                || text.contains(
                                                "mca")
                                || text.contains(
                                                "btech")
                                || text.contains(
                                                "degree")
                                || text.contains(
                                                "college")
                                || text.contains(
                                                "university");
        }

        public boolean hasExperience(
                        String resumeText) {

                String text = resumeText.toLowerCase();

                return text.contains(
                                "experience")
                                || text.contains(
                                                "internship")
                                || text.contains(
                                                "intern")
                                || text.contains(
                                                "worked");
        }

        public boolean isITResume(List<String> skills) {

                return !skills.isEmpty();
        }

        public List<ResumeAnalysis> getHistory(
                        String email) {

                return resumeAnalysisRepository
                                .findByUserEmail(
                                                email);
        }

        public ResumeAnalysis getLatestAnalysis(
                        String email) {

                return resumeAnalysisRepository
                                .findTopByUserEmailOrderByIdDesc(
                                                email);
        }
}
