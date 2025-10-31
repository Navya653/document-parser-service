package com.exituity.documentparser.service;

import com.exituity.documentparser.model.ParsedDocument;
import com.exituity.documentparser.model.PdfParsedData;
import com.exituity.documentparser.parser.Parser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DocumentService {

    private final List<Parser> parsers;

    public DocumentService(List<Parser> parsers) {
        this.parsers = parsers;
    }

    public Object parseDocument(MultipartFile file) throws Exception {
        // detect parser based on file type
        Parser parser = parsers.stream()
                .filter(p -> p.canParse(file.getContentType(), file.getOriginalFilename()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(" No parser found for file: " + file.getOriginalFilename()));

        // parse it (returns Object)
        Object result = parser.parse(file);

        // handle each type (optional)
        if (result instanceof ParsedDocument parsedDoc) {
            System.out.println(" Parsed as generic document");
            return parsedDoc;
        } else if (result instanceof PdfParsedData pdfDoc) {
            System.out.println(" Parsed as PDF document");
            return pdfDoc;
        } else {
            throw new RuntimeException(" Unknown parser result type: " + result.getClass());
        }
    }
}
