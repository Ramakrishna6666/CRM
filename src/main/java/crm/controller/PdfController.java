package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    private final PdfService pdfService;
    private final S3Client s3Client;
    
    @Value("${aws.s3.bucket.name:crm-pdf-bucket}")
    private String bucketName;

    public PdfController(PdfService pdfService, S3Client s3Client) {
        this.pdfService = pdfService;
        this.s3Client = s3Client;
    }

    /**
     * Generate PDF and upload to S3 instead of local file system.
     * This ensures data durability and availability in cloud environments.
     * 
     * @param fileName Name of the PDF file
     * @param text Content to be written in PDF
     * @return S3 object key where PDF was stored
     * @throws IOException if S3 upload fails
     * @throws DocumentException if PDF generation fails
     */
    private String generateAndUploadPdfToS3(String fileName, String text) throws IOException, DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        // Generate PDF in memory instead of writing to local file system
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Upload to S3 for durable storage
            byte[] pdfBytes = outputStream.toByteArray();
            String s3Key = "pdfs/" + fileName;
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType("application/pdf")
                    .build();
            
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(pdfBytes));
            log.info("PDF successfully uploaded to S3: bucket={}, key={}", bucketName, s3Key);
            
            return s3Key;
        } catch (S3Exception e) {
            log.error("Failed to upload PDF to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload PDF to S3", e);
        } finally {
            outputStream.close();
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
            } catch (IOException e) {
                log.error("Failed to upload PDF to S3", e);
                model.addAttribute("error", "Failed to upload PDF to cloud storage");
                return "pdf/generator";
            } catch (DocumentException e) {
                log.error("Failed to generate PDF document", e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }

}
