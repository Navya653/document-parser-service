# 🧠 Document Parser Service

A Spring Boot microservice that extracts and structures text, tables, and metadata from multiple document formats — including **PDFs, Word files, Excel sheets, and Images (OCR)** — into clean, machine-readable **JSON**.

---

## 📄 Project Overview

The purpose of this project is to build a **Document Parsing Module** that accepts unstructured files and outputs structured, standardized JSON.  
It allows downstream systems to easily consume, analyze, or store document content.

**Supported File Types**
- 📄 PDF — Text extraction via Apache PDFBox  
- 🧠 Word (.docx) — Text and tables via Apache Tika + POI  
- 📊 Excel (.xlsx, .xls) — Structured tables via Apache POI  
- 🖼 Image (.jpg, .png, .tiff) — OCR-based text extraction via Tesseract (Tess4J)

---

## ⚙️ Technologies Used

| Technology | Purpose |
|-------------|----------|
| **Spring Boot 3.5.0** | REST API framework |
| **Apache PDFBox** | PDF text extraction |
| **Apache POI** | Excel & Word parsing |
| **Apache Tika** | Auto-detect file types & fallback parser |
| **Tess4J (Tesseract OCR)** | OCR engine for image-based documents |
| **SLF4J + Logback** | Logging framework |
| **Jakarta Validation** | Validating uploaded files |

All dependencies are managed through **Maven** (`pom.xml`).

---

## 🧩 Design Choices

1. **Modular Architecture**  
   Each file type has its own parser (PDF, Word, Excel, Image) implementing a shared `Parser` interface.

2. **Separation of Concerns**  
   - `controller` → Handles upload requests  
   - `service` → Chooses the correct parser  
   - `parser` → File-type specific logic  
   - `model` → Holds `ParsedDocument`, `Metadata`, and `Table` data classes

3. **Unified JSON Output**  
   All parsers return consistent output like:
   ```json
   {
     "text": "...",
     "tables": [...],
     "metadata": {...}
   }
4. **Error Handling & Extensibility**  
   Each parser has robust exception handling and can easily be extended to support new formats (e.g. CSV, TXT).

---

## 🧰 Assumptions

- Files are readable and non-encrypted.  
- Image parsing requires **Tesseract OCR** installed locally.  
- Page count for images and Word documents is assumed as `1`.  
- Confidence scores are approximate and depend on file quality.

---

## 🖼 Setting Up Tesseract OCR (Windows)

For image OCR support:

1. Download **Tesseract OCR** from [UB Mannheim Builds](https://github.com/UB-Mannheim/tesseract/wiki).  
2. Install it (example path: `D:\MyWork\Tesseract-OCR`).  
3. In your `ImageParser.java`, set the data path:
   ```java
   tesseract.setDatapath("D:/MyWork/Tesseract-OCR/tessdata");
4. Verify setup by running in CMD:
   ```bash
   tesseract --version


## 🚀 How to Run the Application

### 🧩 Requirements
- Java **17+**
- Maven **3.9+**
- (Optional) **Tesseract OCR** installed for image parsing

---

### ⚙️ Run Commands
```bash
# Clean and build the project
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run

Once the application starts, open your browser or API client and visit:
👉 http://localhost:8080/api/v1/parse

API Usage Example

Endpoint:
POST /api/v1/parse
Content-Type: multipart/form-data

Form Field:
file=<your document>

Sample Response (for PDF):
{
  "text": {
    "pages": [
      {
        "pageNumber": 1,
        "text": "Invoice No: INV-1003...",
        "tables": []
      }
    ]
  },
  "metadata": {
    "fileName": "invoice-sample.pdf",
    "fileType": "application/pdf",
    "extractedBy": "PDFBox + OCR",
    "extractionConfidence": 0.95
  }
}

| File Type    | Example File         | Output Type     |
| ------------ | -------------------- | --------------- |
|  **PDF**   | `invoice-sample.pdf` | Text + Tables   |
|  **Word**  | `sample-report.docx` | Text + Tables   |
|  **Excel** | `sales-data.xlsx`    | Structured Rows |
|  **Image** | `quote.jpeg`         | OCR Text        |

📁 Example input and output files are included in the /examples folder of this repository for easy verification.


Highlights

✅ Modular and extensible design
✅ Supports multiple file formats
✅ Includes OCR for images
✅ Produces clean, consistent JSON
✅ Built using open-source, enterprise-grade libraries

👩‍💻 Author

Navya Doddapneni
💼 Java Full Stack Developer | Spring Boot | React | MySQL

