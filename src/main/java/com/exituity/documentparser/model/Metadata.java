package com.exituity.documentparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata — file-level details for traceability.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Metadata {
    private String fileName;
    private String fileType;
    private long fileSize;
    private int pageCount;
    private double extractionConfidence;
    private String extractedBy;
    private String parseDate;
}
