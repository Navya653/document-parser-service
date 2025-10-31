package com.exituity.documentparser.parser;

import com.exituity.documentparser.model.Metadata;
import com.exituity.documentparser.model.ParsedDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 *  PdfParser — Extracts text from PDF files (supports text and scanned images via OCR)
 */
@Component
public class PdfParser implements Parser {

    @Override
    public boolean canParse(String contentType, String filename) {
        if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) return true;
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        ParsedDocument parsed = new ParsedDocument();

        try (InputStream is = file.getInputStream();
             PDDocument document = PDDocument.load(is)) {

            int pageCount = document.getNumberOfPages();
            String text;

            //  Extract text using PDFBox
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document).trim();

            //  Fallback to OCR if PDF is scanned (no text found)
            if (text.isBlank()) {
                System.out.println("⚙️ No embedded text found — using OCR mode...");
                text = extractTextWithOcr(document);
            }

            //  Prepare structured JSON (page-based text)
            Map<String, Object> pageObj = new LinkedHashMap<>();
            pageObj.put("pageNumber", 1);
            pageObj.put("text", text);
            pageObj.put("tables", Collections.emptyList());

            Map<String, Object> textWrapper = new LinkedHashMap<>();
            textWrapper.put("pages", Collections.singletonList(pageObj));

            //  Metadata
            Metadata metadata = new Metadata(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    pageCount,
                    0.95,
                    "PDFBox + OCR",
                    DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            );

            parsed.setText(textWrapper);
            parsed.setTables(Collections.emptyList());
            parsed.setMetadata(metadata);

        } catch (Exception e) {
            System.err.println("❌ PDF parsing failed: " + e.getMessage());
            throw e;
        }

        return parsed;
    }

    /**
     * OCR fallback for image-based PDFs using Tesseract.
     */
    private String extractTextWithOcr(PDDocument document) {
        StringBuilder ocrText = new StringBuilder();
        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage("eng");

        //  Auto-detect Tesseract path (Windows / Mac / Linux)
        try {
            String os = System.getProperty("os.name").toLowerCase();
            File tessPath;

            if (os.contains("win")) {
                tessPath = new File("C:\\Program Files\\Tesseract-OCR\\tessdata");
                if (!tessPath.exists()) {
                    tessPath = new File("C:\\Users\\" + System.getProperty("user.name") + "\\Tesseract-OCR\\tessdata");
                }
            } else if (os.contains("mac")) {
                tessPath = new File("/usr/local/share/tessdata");
            } else {
                tessPath = new File("/usr/share/tesseract-ocr/4.00/tessdata");
            }

            if (tessPath.exists()) {
                tesseract.setDatapath(tessPath.getAbsolutePath());
                System.out.println("✅ Using Tesseract datapath: " + tessPath);
            } else {
                System.err.println("⚠️ Warning: Tesseract datapath not found. OCR may fail.");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Failed to set Tesseract path: " + e.getMessage());
        }

        try {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300, ImageType.RGB);
                try {
                    String result = tesseract.doOCR(image);
                    ocrText.append("\n=== OCR Page ").append(i + 1).append(" ===\n")
                           .append(result.trim()).append("\n");
                } catch (TesseractException e) {
                    System.err.println("⚠️ OCR failed on page " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ OCR extraction error: " + e.getMessage());
        }

        return ocrText.toString().trim();
    }
}
