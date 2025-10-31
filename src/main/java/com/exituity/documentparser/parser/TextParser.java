package com.exituity.documentparser.parser;

import com.exituity.documentparser.model.Metadata;
import com.exituity.documentparser.model.ParsedDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TextParser implements Parser {

    private static final Set<String> TEXT_TYPES = Set.of(
            "text/plain", "text/csv", "text/html",
            "application/xml", "application/json", "text/markdown"
    );

    @Override
    public boolean canParse(String contentType, String filename) {
        if (contentType != null && TEXT_TYPES.contains(contentType.toLowerCase())) return true;

        if (filename != null) {
            String lower = filename.toLowerCase();
            return lower.endsWith(".txt") || lower.endsWith(".csv") || lower.endsWith(".log")
                    || lower.endsWith(".html") || lower.endsWith(".xml")
                    || lower.endsWith(".json") || lower.endsWith(".md");
        }
        return false;
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or invalid");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // Limit: 10 MB
            throw new IllegalArgumentException("Text file too large for parsing (>10MB)");
        }

        String text;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            text = reader.lines().collect(Collectors.joining("\n"));
        }

        Metadata metadata = new Metadata(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                1,
                0.99,
                "TextParser (BufferedReader)",
                DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        );

        ParsedDocument parsed = new ParsedDocument();
        parsed.setText(text);
        parsed.setTables(Collections.emptyList());
        parsed.setMetadata(metadata);

        return parsed;
    }
}
