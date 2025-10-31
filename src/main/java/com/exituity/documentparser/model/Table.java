package com.exituity.documentparser.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Table — represents structured tabular data extracted from documents.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Table {
    private String tableName;
    private List<String> headers;
    private List<List<String>> rows;
    private List<Object> structuredRows;
    private double confidenceScore;
}
