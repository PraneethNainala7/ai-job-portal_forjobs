package com.aijobportal.ai.client;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Component
public class ResumeTextExtractor {

    private static final Logger log = LoggerFactory.getLogger(ResumeTextExtractor.class);
    private static final int MAX_CHARS = 40_000;

    public String extract(String fileUrl, String fileName) throws IOException {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IOException("Resume file path is missing.");
        }
        Path path = Path.of(fileUrl);
        if (!Files.isRegularFile(path)) {
            throw new IOException("Resume file was not found on disk.");
        }
        String name = fileName == null ? path.getFileName().toString() : fileName;
        String lower = name.toLowerCase(Locale.ROOT);
        String text;
        if (lower.endsWith(".pdf")) {
            text = fromPdf(path);
        } else if (lower.endsWith(".docx")) {
            text = fromDocx(path);
        } else if (lower.endsWith(".doc")) {
            throw new IOException("DOC files are not supported. Upload a text-based PDF or DOCX.");
        } else {
            throw new IOException("Upload a text-based PDF or DOCX resume.");
        }
        String cleaned = text == null ? "" : text.replace('\u0000', ' ').trim();
        if (cleaned.isBlank()) {
            throw new IOException("Could not read text from this resume. Use a text-based PDF or DOCX.");
        }
        if (cleaned.length() > MAX_CHARS) {
            return cleaned.substring(0, MAX_CHARS);
        }
        return cleaned;
    }

    private static String fromPdf(Path path) throws IOException {
        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static String fromDocx(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    public String extractOrEmpty(String fileUrl, String fileName) {
        try {
            return extract(fileUrl, fileName);
        } catch (IOException ex) {
            log.warn("Could not extract resume text: {}", ex.getMessage());
            return "";
        }
    }
}
