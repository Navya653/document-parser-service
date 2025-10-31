package com.exituity.documentparser.parser;

import com.exituity.documentparser.model.Metadata;
import com.exituity.documentparser.model.ParsedDocument;
import com.exituity.documentparser.model.Table;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ExcelParser implements Parser {

    private static final Set<String> SUPPORTED_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
            "application/vnd.ms-excel" // .xls
    );

    @Override
    public boolean canParse(String contentType, String filename) {
        if (contentType != null && SUPPORTED_TYPES.contains(contentType.toLowerCase())) return true;

        if (filename != null) {
            String lower = filename.toLowerCase();
            return lower.endsWith(".xlsx") || lower.endsWith(".xls");
        }
        return false;
    }

    @Override
    public ParsedDocument parse(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded Excel file is empty or invalid.");
        }

        List<Map<String, Object>> sheetList = new ArrayList<>();
        List<Table> allTables = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            for (Sheet sheet : workbook) {
                List<List<String>> rows = new ArrayList<>();

                for (Row row : sheet) {
                    List<String> cells = new ArrayList<>();

                    for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                        Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        cell.setCellType(CellType.STRING);
                        cells.add(cell.getStringCellValue().trim());
                    }

                    if (cells.stream().anyMatch(v -> !v.isBlank())) {
                        rows.add(cells);
                    }
                }

                if (!rows.isEmpty()) {
                    // Derive headers from first row if meaningful
                    List<String> headers = new ArrayList<>(rows.get(0));
                    List<List<String>> dataRows = rows.size() > 1 ? rows.subList(1, rows.size()) : Collections.emptyList();

                 // use no-arg constructor then setters (if you prefer)
                    Table table = new Table();
                    table.setHeaders(headers);
                    table.setRows(dataRows);
                    table.setStructuredRows(new ArrayList<>());
                    allTables.add(table);

                    // Prepare structured sheet output
                    Map<String, Object> sheetObj = new LinkedHashMap<>();
                    sheetObj.put("sheetName", sheet.getSheetName());
                    sheetObj.put("headers", headers);
                    sheetObj.put("rows", dataRows);
                    sheetList.add(sheetObj);
                }
            }

            // Wrap output
            Map<String, Object> textWrapper = new LinkedHashMap<>();
            textWrapper.put("sheets", sheetList);

            Metadata metadata = new Metadata(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    workbook.getNumberOfSheets(),
                    0.97,
                    "Apache POI (ExcelParser)",
                    DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            );

            ParsedDocument parsed = new ParsedDocument();
            parsed.setText(textWrapper);
            parsed.setTables(allTables);
            parsed.setMetadata(metadata);

            return parsed;
        } catch (Exception e) {
            throw new RuntimeException(" Excel parsing failed: " + e.getMessage(), e);
        }
    }
}
