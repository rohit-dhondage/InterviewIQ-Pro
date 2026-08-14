package com.example.Interview.resume;

import com.example.Interview.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class ResumeTextExtractor {

    private static final int MAX_CHARS_FOR_SCORING = 12_000;

    /**
     * Extracts plain text from a PDF's raw bytes, truncated to a size safe
     * for the chat model's context window.
     */
    public String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            String text = new PDFTextStripper().getText(document);
            if (text == null || text.isBlank()) {
                throw new ApiException(
                        "Couldn't read any text from this PDF — it may be a scanned image rather than a text-based resume",
                        HttpStatus.UNPROCESSABLE_ENTITY);
            }
            return text.length() > MAX_CHARS_FOR_SCORING
                    ? text.substring(0, MAX_CHARS_FOR_SCORING)
                    : text;
        } catch (IOException e) {
            log.error("Failed to parse PDF", e);
            throw new ApiException("Could not parse this PDF file", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}