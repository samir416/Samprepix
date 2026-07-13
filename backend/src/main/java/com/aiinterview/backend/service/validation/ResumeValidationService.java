package com.aiinterview.backend.service.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class ResumeValidationService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx");

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Value("${resume.max-file-size:10485760}")
    private long maxFileSize;

    /**
     * Validates the uploaded resume.
     *
     * @param file uploaded resume file
     */
    public void validate(MultipartFile file) {

        validateFileExists(file);

        validateFileNotEmpty(file);

        validateFileSize(file);

        validateExtension(file);

        validateContentType(file);
    }

    /**
     * Ensures file object is present.
     */
    private void validateFileExists(MultipartFile file) {

        if (file == null) {
            throw new IllegalArgumentException("Resume file is required.");
        }
    }

    /**
     * Ensures uploaded file is not empty.
     */
    private void validateFileNotEmpty(MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded resume is empty.");
        }
    }

    /**
     * Validates maximum file size.
     */
    private void validateFileSize(MultipartFile file) {

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    "Resume size exceeds maximum allowed limit of "
                            + (maxFileSize / (1024 * 1024))
                            + " MB."
            );
        }
    }

    /**
     * Validates file extension.
     */
    private void validateExtension(MultipartFile file) {

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("Invalid resume filename.");
        }

        String extension = filename.substring(filename.lastIndexOf('.') + 1)
                .toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Only PDF and DOCX resumes are supported."
            );
        }
    }

    /**
     * Validates MIME type.
     */
    private void validateContentType(MultipartFile file) {

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new IllegalArgumentException(
                    "Invalid resume content type."
            );
        }
    }
}