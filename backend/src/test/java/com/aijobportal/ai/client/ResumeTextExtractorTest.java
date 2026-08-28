package com.aijobportal.ai.client;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeTextExtractorTest {

    @TempDir
    Path tempDir;

    private final ResumeTextExtractor extractor = new ResumeTextExtractor();

    @Test
    void extractsTextFromPdf() throws Exception {
        Path pdf = tempDir.resolve("resume.pdf");
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText("Java Spring Boot");
                stream.endText();
            }
            document.save(pdf.toFile());
        }
        String text = extractor.extract(pdf.toString(), "resume.pdf");
        assertTrue(text.contains("Java Spring Boot"));
    }

    @Test
    void extractsTextFromDocx() throws Exception {
        Path docx = tempDir.resolve("resume.docx");
        try (XWPFDocument document = new XWPFDocument()) {
            document.createParagraph().createRun().setText("React TypeScript");
            document.write(java.nio.file.Files.newOutputStream(docx));
        }
        String text = extractor.extract(docx.toString(), "resume.docx");
        assertTrue(text.contains("React TypeScript"));
    }

    @Test
    void rejectsLegacyDoc() throws Exception {
        Path doc = tempDir.resolve("resume.doc");
        java.nio.file.Files.writeString(doc, "not a real doc");
        assertTrue(extractor.extractOrEmpty(doc.toString(), "resume.doc").isBlank());
        assertThrows(Exception.class, () -> extractor.extract(doc.toString(), "resume.doc"));
    }
}
