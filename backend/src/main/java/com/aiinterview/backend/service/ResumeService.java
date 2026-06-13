package com.aiinterview.backend.service;

import com.aiinterview.backend.entity.ResumeAnalysis;
import com.aiinterview.backend.repository.ResumeAnalysisRepository;

import java.time.LocalDateTime;

import com.aiinterview.backend.model.ResumeResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

@Service
public class ResumeService {

        public ResumeResponse analyzeResume() {

                ResumeResponse response = new ResumeResponse();

                response.setScore(78);

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
                        MultipartFile file) throws IOException {

                PDDocument document = Loader.loadPDF(
                                file.getBytes());

                PDFTextStripper stripper = new PDFTextStripper();

                String text = stripper.getText(document);

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
                                "c",
                                "c++"
                };

                for (String skill : skills) {

                        if (text.contains(
                                        skill.toLowerCase())) {

                                detectedSkills.add(skill);
                        }
                }

                return detectedSkills;
        }

        public int calculateScore(
                        List<String> skills) {

                int totalSkills = 10;

                return (skills.size() * 40)
                                / totalSkills;
        }

        public List<String> findMissingSkills(
                        List<String> detectedSkills) {

                List<String> requiredSkills = List.of(
                                "java",
                                "sql",
                                "html",
                                "css",
                                "javascript",
                                "react",
                                "spring boot",
                                "mysql");

                List<String> missingSkills = new ArrayList<>();

                for (String skill : requiredSkills) {

                        if (!detectedSkills.contains(skill)) {

                                missingSkills.add(skill);
                        }
                }

                return missingSkills;
        }

        public List<String> generateSuggestions(
                        List<String> missingSkills) {

                List<String> suggestions = new ArrayList<>();

                for (String skill : missingSkills) {

                        switch (skill) {

                                case "react" ->
                                        suggestions.add(
                                                        "Learn React and build frontend projects");

                                case "spring boot" ->
                                        suggestions.add(
                                                        "Add Spring Boot backend projects");

                                case "javascript" ->
                                        suggestions.add(
                                                        "Improve JavaScript fundamentals");

                                case "mysql" ->
                                        suggestions.add(
                                                        "Practice MySQL queries and database design");

                                default ->
                                        suggestions.add(
                                                        "Improve " + skill + " skills");
                        }
                }

                return suggestions;
        }

        public ResumeResponse analyzeResumeFile(MultipartFile file, String email) throws Exception {

                String text = extractTextFromPdf(file);

                List<String> skills = detectSkills(text);

                int score = calculateScore(skills);

                /* Projects */

                if (hasProjects(text)) {

                        score += 20;
                }

                /* Education */

                if (hasEducation(text)) {

                        score += 10;
                }

                /* Experience */

                if (hasExperience(text)) {

                        score += 10;
                }

                /* GitHub */

                if (hasGithub(text)) {

                        score += 10;
                }

                /* LinkedIn */

                if (hasLinkedIn(text)) {

                        score += 10;
                }

                if (score > 100) {

                        score = 100;
                }

                List<String> missingSkills = findMissingSkills(skills);

                ResumeResponse response = new ResumeResponse();

                response.setScore(score);

                response.setSkills(skills);

                response.setMissingSkills(
                                missingSkills);

                response.setSuggestions(
                                generateSuggestions(
                                                missingSkills));
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

                analysis.setUserEmail(email);

                analysis.setScore(score);

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
                                .save(analysis);

                return response;
        }

        public boolean hasGithub(
                        String resumeText) {

                return resumeText
                                .toLowerCase()
                                .contains("github");
        }

        public boolean hasLinkedIn(
                        String resumeText) {

                return resumeText
                                .toLowerCase()
                                .contains("linkedin");
        }

        public boolean hasProjects(
                        String resumeText) {

                String text = resumeText.toLowerCase();

                return text.contains("project")
                                || text.contains("projects")
                                || text.contains("developed")
                                || text.contains("built");
        }

        public boolean hasEducation(
                        String resumeText) {

                String text = resumeText.toLowerCase();

                return text.contains("bca")
                                || text.contains("bsc")
                                || text.contains("mca")
                                || text.contains("btech")
                                || text.contains("degree")
                                || text.contains("college")
                                || text.contains("university");
        }

        public boolean hasExperience(
                        String resumeText) {

                String text = resumeText.toLowerCase();

                return text.contains("experience")
                                || text.contains("internship")
                                || text.contains("intern")
                                || text.contains("worked");
        }

        private final ResumeAnalysisRepository resumeAnalysisRepository;

        public ResumeService(
                        ResumeAnalysisRepository resumeAnalysisRepository) {

                this.resumeAnalysisRepository = resumeAnalysisRepository;
        }

        public List<ResumeAnalysis> getHistory(String email) {

                return resumeAnalysisRepository.findByUserEmail(email);
        }

        public ResumeAnalysis getLatestAnalysis(String email) {

                return resumeAnalysisRepository
                                .findTopByUserEmailOrderByIdDesc(email);
        }

}