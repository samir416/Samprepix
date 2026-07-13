package com.aiinterview.backend.service.parser;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfParserService {

    /**
     * Extracts text from a PDF resume.
     *
     * @param file uploaded PDF file
     * @return extracted and normalized text
     */
    public String extractText(MultipartFile file) {

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();

            String extractedText = stripper.getText(document);

            return normalizeText(extractedText);

        } catch (IOException exception) {
            throw new RuntimeException("Failed to extract text from PDF resume.", exception);
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