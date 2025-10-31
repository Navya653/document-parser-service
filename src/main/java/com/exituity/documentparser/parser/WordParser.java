package com.exituity.documentparser.parser;

import com.exituity.documentparser.model.Metadata;
import com.exituity.documentparser.model.ParsedDocument;
import com.exituity.documentparser.model.Table;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 🧠 WordParser — extracts text and tables from Word files (.docx, .doc)
 * Uses Apache Tika for text extraction and Apache POI for table extraction.
 */
@Component
public class WordParser implements Parser {

    @Override
    public boolean canParse(String contentType, String filename) {
        if (contentType != null && contentType.contains("word"))
            return true;
        return filename != null && (filename.endsWith(".docx") || filename.endsWith(".doc"));
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        ParsedDocument parsed = new ParsedDocument();

        try (InputStream is = file.getInputStream()) {

            // Step 1: Extract text using Apache Tika
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            org.apache.tika.metadata.Metadata tikaMetadata = new org.apache.tika.metadata.Metadata();

            parser.parse(is, handler, tikaMetadata);
            String text = handler.toString().trim();

            //  Step 2: Extract tables using Apache POI (for .docx files)
            List<Table> tables = new ArrayList<>();
            if (file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".docx")) {
                tables = extractTablesUsingPOI(file);
            }

            //  Step 3: Build metadata (7 parameters)
            Metadata metadata = new Metadata(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    1,                      // Page count approximation for Word
                    0.85,                   // Confidence
                    "Apache Tika (AutoDetectParser)",
                    DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            );

            //  Step 4: Populate ParsedDocument
            parsed.setText(text);
            parsed.setTables(tables);
            parsed.setMetadata(metadata);
            parsed.setExtractedImages(null);
            parsed.setIssues(null);

        } catch (Exception e) {
            System.err.println("❌ Word parsing failed: " + e.getMessage());
            throw e;
        }

        return parsed;
    }

    /**
     *  Extract structured tables from DOCX using Apache POI.
     */
    private List<Table> extractTablesUsingPOI(MultipartFile file) {
        List<Table> tableList = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             XWPFDocument doc = new XWPFDocument(is)) {

            for (XWPFTable table : doc.getTables()) {
                List<List<String>> rows = new ArrayList<>();

                for (XWPFTableRow row : table.getRows()) {
                    List<String> cells = new ArrayList<>();
                    for (XWPFTableCell cell : row.getTableCells()) {
                        cells.add(cell.getText().trim());
                    }
                    rows.add(cells);
                }

                if (rows.isEmpty()) continue;

                // Detect headers
                List<String> headers = new ArrayList<>();
                if (!rows.isEmpty()) headers = rows.remove(0);

                //  Use setter-based Table creation (no constructor conflict)
                Table t = new Table();
                t.setTableName(null);
                t.setHeaders(headers);
                t.setRows(rows);
                t.setStructuredRows(new ArrayList<>());
                t.setConfidenceScore(0.95);

                tableList.add(t);
            }

        } catch (Exception e) {
            System.err.println(" Table extraction failed in Word: " + e.getMessage());
        }

        return tableList;
    }
}
