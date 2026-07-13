package com.aiinterview.backend.service.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class DocxParserService {

    /**
     * Extracts text from a DOCX resume.
     *
     * @param file uploaded DOCX file
     * @return extracted and normalized text
     */
    public String extractText(MultipartFile file) {

        try (
                XWPFDocument document = new XWPFDocument(file.getInputStream());
                XWPFWordExtractor extractor = new XWPFWordExtractor(document)
        ) {

            String extractedText = extractor.getText();

            return normalizeText(extractedText);

        } catch (IOException exception) {
            throw new RuntimeException("Failed to extract text from DOCX resume.", exception);
        }
    }

    /**
     * Normalizes extracted text for AI processing.
     *
     * @param text raw extracted text
     * @return cleaned text
     */
    private String normalizeText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\r", " ")
                .replace("\n", " ")
                .replace("\t", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}