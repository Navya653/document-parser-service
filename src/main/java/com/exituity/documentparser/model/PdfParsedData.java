package com.exituity.documentparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Specialized model for parsed PDF output.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfParsedData {

    private PdfText text;
    private List<Table> tables;
    private Metadata metadata;
    private List<String> extractedImages;
    private List<String> issues;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PdfText {
        private List<Page> pages;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Page {
        private int pageNumber;
        private String text;
        private List<Table> tables;
    }
}
