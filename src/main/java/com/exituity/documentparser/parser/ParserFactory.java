package com.exituity.documentparser.parser;

import org.springframework.stereotype.Component;
import java.util.List;

/**
 *  ParserFactory — Centralized registry for all available parsers.
 *
 * Dynamically selects the correct parser implementation (PDF, Excel, Image, etc.)
 * based on file type or file extension.
 *
 * Registered automatically by Spring since all parser implementations
 * are annotated with @Component.
 */
@Component
public class ParserFactory {

    private final List<Parser> parsers;

    // Spring injects all Parser beans here
    public ParserFactory(List<Parser> parsers) {
        this.parsers = parsers;
    }

    /**
     * Selects the appropriate parser for the given file.
     *
     * @param contentType MIME type (e.g., application/pdf)
     * @param filename    Original uploaded filename
     * @return Matching Parser implementation
     * @throws UnsupportedOperationException if no parser matches
     */
    public Parser getParser(String contentType, String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }

        for (Parser parser : parsers) {
            try {
                if (parser.canParse(contentType, filename)) {
                    return parser;
                }
            } catch (Exception e) {
                System.err.println("Error checking parser " + parser.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        throw new UnsupportedOperationException("Unsupported file type: " + filename + " (" + contentType + ")");
    }
}
