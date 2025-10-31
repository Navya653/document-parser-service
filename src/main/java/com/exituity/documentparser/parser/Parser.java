package com.exituity.documentparser.parser;

import com.exituity.documentparser.model.ParsedDocument;
import org.springframework.web.multipart.MultipartFile;

/**
 *  Parser Interface — contract for all document parsers.
 */
public interface Parser {

    /**
     * Determines if this parser can handle the given file.
     */
    boolean canParse(String contentType, String filename);

    /**
     * Parses the file into a structured ParsedDocument.
     */
    ParsedDocument parse(MultipartFile file) throws Exception;
}
