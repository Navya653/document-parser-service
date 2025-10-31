package com.exituity.documentparser.parser;

import com.exituity.documentparser.model.Metadata;
import com.exituity.documentparser.model.ParsedDocument;
import org.apache.tika.Tika;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Component
public class TikaParser implements Parser {

    private final Tika tika = new Tika();

    @Override
    public boolean canParse(String contentType, String filename) {
        // Fallback parser — used when others fail
        return true;
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty or invalid.");
        }

        try (InputStream is = file.getInputStream()) {
            AutoDetectParser parser = new AutoDetectParser();
            org.apache.tika.metadata.Metadata tikaMeta = new org.apache.tika.metadata.Metadata();
            BodyContentHandler handler = new BodyContentHandler(-1); // no length limit

            String text;
            try {
                parser.parse(is, handler, tikaMeta);
                text = handler.toString();
            } catch (Exception e) {
                text = "[Tika failed to parse content: " + e.getMessage() + "]";
            }

            double confidence = text.isBlank() ? 0.70 : 0.85;

            Metadata docMetadata = new Metadata(
                    file.getOriginalFilename(),
                    tika.detect(file.getOriginalFilename()),
                    file.getSize(),
                    1,
                    confidence,
                    "Apache Tika (AutoDetectParser)",
                    DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            );

            ParsedDocument parsed = new ParsedDocument();
            parsed.setText(text);
            parsed.setTables(Collections.emptyList());
            parsed.setMetadata(docMetadata);

            return parsed;
        }
    }
}
