package com.exituity.documentparser.parser;

import com.exituity.documentparser.model.Metadata;
import com.exituity.documentparser.model.ParsedDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ImageParser — Extracts text from image files using Tesseract OCR.
 * Supports: PNG, JPG, JPEG, TIFF, BMP.
 */
@Component
public class ImageParser implements Parser {

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/tiff", "image/bmp"
    );

    private final Tesseract tesseract;

    public ImageParser() {
        tesseract = new Tesseract();
        tesseract.setLanguage("eng");

        // 🧩 Auto-detect and set Tesseract path safely
        try {
            String os = System.getProperty("os.name").toLowerCase();
            File tessPath;

            if (os.contains("win")) {
                // ✅ Common Windows installation paths
                tessPath = new File("C:\\Program Files\\Tesseract-OCR\\tessdata");
                if (!tessPath.exists()) {
                    tessPath = new File("C:\\Users\\" + System.getProperty("user.name") + "\\Tesseract-OCR\\tessdata");
                }
            } else if (os.contains("mac")) {
                tessPath = new File("/usr/local/share/tessdata");
            } else {
                // Linux or Unix-like
                tessPath = new File("/usr/share/tesseract-ocr/4.00/tessdata");
            }

            if (tessPath.exists()) {
                tesseract.setDatapath(tessPath.getAbsolutePath());
                System.out.println("Using Tesseract data path: " + tessPath);
            } else {
                System.err.println("Warning: Tesseract data path not found. OCR may fail.");
            }

        } catch (Exception e) {
            System.err.println("Error setting Tesseract datapath: " + e.getMessage());
        }
    }

    @Override
    public boolean canParse(String contentType, String filename) {
        if (contentType != null && IMAGE_TYPES.contains(contentType)) return true;
        if (filename != null) {
            String f = filename.toLowerCase();
            return f.endsWith(".png") || f.endsWith(".jpg") || f.endsWith(".jpeg")
                    || f.endsWith(".tif") || f.endsWith(".bmp");
        }
        return false;
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        ParsedDocument parsed = new ParsedDocument();

        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IllegalArgumentException("Unsupported or corrupt image file: " + file.getOriginalFilename());
            }

            // 🧠 Extract text using OCR
            String text;
            try {
                text = tesseract.doOCR(image);
            } catch (TesseractException e) {
                text = "";
                System.err.println("OCR failed: " + e.getMessage());
            }

            // 🧾 Wrap text in structured JSON
            Map<String, Object> pageObj = new LinkedHashMap<>();
            pageObj.put("pageNumber", 1);
            pageObj.put("text", text.trim());
            pageObj.put("tables", Collections.emptyList());

            Map<String, Object> textWrapper = new LinkedHashMap<>();
            textWrapper.put("pages", Collections.singletonList(pageObj));

            // 🧩 Metadata
            Metadata metadata = new Metadata(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    1,
                    0.85,
                    "Tesseract OCR",
                    DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            );

            parsed.setText(textWrapper);
            parsed.setTables(Collections.emptyList());
            parsed.setMetadata(metadata);

        } catch (Exception e) {
            System.err.println("Image parsing failed: " + e.getMessage());
            throw e;
        }

        return parsed;
    }
}
