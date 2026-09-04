package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.config.CentralLanguageRegistry;
import com.aiinterview.backend.entity.CodingProblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubSolutionHelper {

    private static final Pattern HEADER_PATTERN = Pattern.compile(
            "(?m)^(?://|#|--|;|%|\\*>|\\(\\*)\\s*={3,}\\s*SOLUTION\\s+(\\d+)\\s*={3,}(?:\\s*\\*\\))?\\s*$"
    );

    public record MergeResult(
            boolean duplicate,
            int solutionNumber,
            String content
    ) {
    }

    public static String sanitizeProblemSlug(String slug, String title) {
        String base = (slug != null && !slug.isBlank()) ? slug : title;
        if (base == null || base.isBlank()) {
            base = "problem";
        }
        String sanitized = base.trim().toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return sanitized.isBlank() ? "problem" : sanitized;
    }

    public static String getSolutionFileName(String language) {
        CentralLanguageRegistry.LanguageSpec spec = CentralLanguageRegistry.get(language);
        String extension = (spec != null && spec.fileExtension() != null && !spec.fileExtension().isBlank())
                ? spec.fileExtension().trim()
                : ".txt";
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }
        return "Solution" + extension;
    }

    public static String getSolutionPath(CodingProblem problem, String language) {
        String slug = sanitizeProblemSlug(
                problem != null ? problem.getSlug() : null,
                problem != null ? problem.getTitle() : null
        );
        return "coding-solutions/" + slug + "/" + getSolutionFileName(language);
    }

    public static String getCommentPrefix(String language) {
        if (language == null || language.isBlank()) {
            return "//";
        }
        String key = language.trim().toLowerCase();
        CentralLanguageRegistry.LanguageSpec spec = CentralLanguageRegistry.get(key);
        if (spec != null) {
            key = spec.key().toLowerCase();
        }

        return switch (key) {
            case "python", "ruby", "bash", "dash", "elixir", "perl", "r", "julia", "raku", "nim", "coffeescript", "awk", "bqn", "dragon", "powershell", "pwsh" -> "#";
            case "lua", "haskell", "mysql", "sql", "sqlite3" -> "--";
            case "racket", "lisp", "clojure", "emacs" -> ";";
            case "erlang", "prolog" -> "%";
            case "cobol" -> "*>";
            case "ocaml" -> "(*";
            case "forth" -> "\\";
            case "basic.net", "freebasic", "vb" -> "'";
            case "fortran", "octave" -> "!";
            default -> "//";
        };
    }

    public static String formatSolutionHeader(int solutionNumber, String language) {
        String prefix = getCommentPrefix(language);
        if ("(*".equals(prefix)) {
            return "(* ================ SOLUTION " + solutionNumber + " ================ *)";
        }
        return prefix + " ================ SOLUTION " + solutionNumber + " ================";
    }

    public static String normalizeCode(String code) {
        if (code == null) {
            return "";
        }
        String unified = code.replace("\r\n", "\n").replace("\r", "\n");
        String[] lines = unified.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i].replaceAll("\\s+$", ""));
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    public static List<String> parseExistingSolutions(String fileContent, String language) {
        List<String> solutions = new ArrayList<>();
        if (fileContent == null || fileContent.isBlank()) {
            return solutions;
        }

        Matcher matcher = HEADER_PATTERN.matcher(fileContent);
        List<Integer> headerStarts = new ArrayList<>();
        List<Integer> headerEnds = new ArrayList<>();

        while (matcher.find()) {
            headerStarts.add(matcher.start());
            headerEnds.add(matcher.end());
        }

        if (headerStarts.isEmpty()) {
            String trimmed = fileContent.trim();
            if (!trimmed.isEmpty()) {
                solutions.add(trimmed);
            }
            return solutions;
        }

        for (int i = 0; i < headerStarts.size(); i++) {
            int start = headerEnds.get(i);
            int end = (i + 1 < headerStarts.size()) ? headerStarts.get(i + 1) : fileContent.length();
            String chunk = fileContent.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                solutions.add(chunk);
            }
        }

        return solutions;
    }

    public static MergeResult prepareMergedContent(String incomingCode, String existingContent, String language) {
        if (incomingCode == null || incomingCode.isBlank()) {
            throw new IllegalArgumentException("Incoming code cannot be empty.");
        }

        List<String> existingSolutions = parseExistingSolutions(existingContent, language);
        String normalizedIncoming = normalizeCode(incomingCode);

        for (int i = 0; i < existingSolutions.size(); i++) {
            String normalizedExisting = normalizeCode(existingSolutions.get(i));
            if (normalizedIncoming.equals(normalizedExisting)) {
                return new MergeResult(true, i + 1, existingContent);
            }
        }

        if (existingSolutions.isEmpty()) {
            return new MergeResult(false, 1, incomingCode.trim() + "\n");
        }

        if (existingSolutions.size() == 1) {
            StringBuilder sb = new StringBuilder();
            sb.append(formatSolutionHeader(1, language)).append("\n\n");
            sb.append(existingSolutions.get(0).trim()).append("\n\n");
            sb.append(formatSolutionHeader(2, language)).append("\n\n");
            sb.append(incomingCode.trim()).append("\n");
            return new MergeResult(false, 2, sb.toString());
        }

        int nextNumber = existingSolutions.size() + 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < existingSolutions.size(); i++) {
            sb.append(formatSolutionHeader(i + 1, language)).append("\n\n");
            sb.append(existingSolutions.get(i).trim()).append("\n\n");
        }
        sb.append(formatSolutionHeader(nextNumber, language)).append("\n\n");
        sb.append(incomingCode.trim()).append("\n");
        return new MergeResult(false, nextNumber, sb.toString());
    }
}
