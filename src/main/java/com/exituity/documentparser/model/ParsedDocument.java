package com.exituity.documentparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * ParsedDocument — Unified representation of any parsed file.
 * Includes extracted text, tables, metadata, images, and issues.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedDocument {
    private Object text;                 // Can hold Map<String, Object> or plain String
    private List<Table> tables;          // Extracted tables
    private Metadata metadata;           // File metadata
    private List<Object> extractedImages; // Optional (for image extraction)
    private List<String> issues;         // Errors or warnings during parsing
}
