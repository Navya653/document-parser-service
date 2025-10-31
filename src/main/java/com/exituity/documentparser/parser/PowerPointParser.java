package com.exituity.documentparser.parser;

import com.exituity.documentparser.model.Metadata;
import com.exituity.documentparser.model.ParsedDocument;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class PowerPointParser implements Parser {

    @Override
    public boolean canParse(String contentType, String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".pptx") || lower.endsWith(".ppt")
                || (contentType != null && (contentType.contains("presentation") || contentType.contains("powerpoint")));
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        ParsedDocument parsed = new ParsedDocument();
        List<Map<String, Object>> slidesList = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            // Handle modern PPTX format
            if (file.getOriginalFilename().toLowerCase().endsWith(".pptx")) {
                try (XMLSlideShow pptx = new XMLSlideShow(is)) {
                    int slideNum = 1;
                    for (XSLFSlide slide : pptx.getSlides()) {
                        List<String> texts = new ArrayList<>();

                        for (XSLFShape shape : slide.getShapes()) {
                            if (shape instanceof XSLFTextShape textShape) {
                                String text = textShape.getText().trim();
                                if (!text.isEmpty()) texts.add(text);
                            }
                        }

                        Map<String, Object> slideData = new LinkedHashMap<>();
                        slideData.put("slideNumber", slideNum++);
                        slideData.put("text", String.join("\n", texts));
                        slidesList.add(slideData);
                    }

                    parsed.setMetadata(new Metadata(
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getSize(),
                            pptx.getSlides().size(),
                            0.93,
                            "Apache POI (XSLF)",
                            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    ));
                }
            }
            // Handle legacy PPT format
            else {
                try (HSLFSlideShow ppt = new HSLFSlideShow(is)) {
                    int slideNum = 1;
                    for (HSLFSlide slide : ppt.getSlides()) {
                        StringBuilder sb = new StringBuilder();
                        if (slide.getTitle() != null) {
                            sb.append(slide.getTitle()).append("\n");
                        }

                        // Extract text paragraphs (safe for all POI versions)
                        for (List<?> paragraphGroup : slide.getTextParagraphs()) {
                            for (Object paragraph : paragraphGroup) {
                                String textPart = paragraph.toString().trim();
                                if (!textPart.isEmpty()) {
                                    sb.append(textPart).append("\n");
                                }
                            }
                        }

                        Map<String, Object> slideData = new LinkedHashMap<>();
                        slideData.put("slideNumber", slideNum++);
                        slideData.put("text", sb.toString().trim());
                        slidesList.add(slideData);
                    }

                    parsed.setMetadata(new Metadata(
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getSize(),
                            ppt.getSlides().size(),
                            0.90,
                            "Apache POI (HSLF)",
                            DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                    ));
                }
            }

            // Wrap slides in structured JSON
            Map<String, Object> textWrapper = new LinkedHashMap<>();
            textWrapper.put("slides", slidesList);

            parsed.setText(textWrapper);
            parsed.setTables(Collections.emptyList());

        } catch (Exception e) {
            throw new RuntimeException("PowerPoint parsing failed: " + e.getMessage(), e);
        }

        return parsed;
    }
}
