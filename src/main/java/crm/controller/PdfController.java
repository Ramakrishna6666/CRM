package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Controller
@Slf4j
public class PdfController {

    private static final String S3_BUCKET_NAME = System.getenv().getOrDefault("S3_BUCKET_NAME", "crm-pdf-bucket");
    private static final String S3_PDF_PREFIX = System.getenv().getOrDefault("S3_PDF_PREFIX", "pdfs/");

    private PdfService pdfService;
    
    @Autowired(required = false)
    private S3Client s3Client;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generates a PDF and uploads it to Amazon S3 instead of writing to local file system.
     * This ensures data durability and availability in cloud environments.
     * 
     * @param fileName The name of the PDF file
     * @param text The content to include in the PDF
     * @return The S3 key where the PDF was stored
     * @throws DocumentException if PDF generation fails
     * @throws IOException if S3 upload fails
     */
    private String generateAndUploadPdfToS3(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        // Generate PDF in memory instead of writing to local file system
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, baos);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Upload to S3
            String s3Key = S3_PDF_PREFIX + fileName;
            byte[] pdfBytes = baos.toByteArray();
            
            if (s3Client != null) {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(S3_BUCKET_NAME)
                        .key(s3Key)
                        .contentType("application/pdf")
                        .contentLength((long) pdfBytes.length)
                        .build();
                
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(pdfBytes));
                log.info("PDF uploaded to S3: s3://{}/{}", S3_BUCKET_NAME, s3Key);
                return s3Key;
            } else {
                log.warn("S3Client not configured. PDF generated but not uploaded.");
                return fileName;
            }
        } catch (S3Exception e) {
            log.error("Failed to upload PDF to S3", e);
            throw new IOException("Failed to upload PDF to S3: " + fileName, e);
        } finally {
            baos.close();
        }
    }

    @GetMapping("/pdf-generator")
    public String pdfGenerator(Model model) {
        model.addAttribute("pdf", new Pdf());
        return "pdf/generator";
    }

    @PostMapping("/pdf-generator")
    public String generatePdf(@Valid Pdf pdf, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "redirect:/pdf-generator";
        } else {
            try {
                String s3Key = generateAndUploadPdfToS3(pdf.getName(), pdf.getContent());
                pdf.setS3Key(s3Key); // Store S3 key instead of local file path
                pdfService.savePdf(pdf);
                model.addAttribute("s3Key", s3Key);
                log.info("PDF generated and saved successfully: {}", s3Key);
            } catch (DocumentException e) {
                log.error("Failed to generate PDF document", e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (IOException e) {
                log.error("Failed to upload PDF to S3", e);
                model.addAttribute("error", "Failed to upload PDF to cloud storage");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }

}
