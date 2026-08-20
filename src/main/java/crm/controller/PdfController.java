package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.PdfService;
import crm.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;
    private S3Service s3Service;

    @Value("${aws.s3.bucket:crm-data-bucket}")
    private String bucketName;

    @Value("${aws.s3.pdf.prefix:pdfs/}")
    private String pdfPrefix;

    public PdfController(PdfService pdfService, S3Service s3Service) {
        this.pdfService = pdfService;
        this.s3Service = s3Service;
    }

    /**
     * Generates a PDF and uploads it to Amazon S3 instead of writing to local file system.
     * This ensures data durability and availability in cloud/containerized environments.
     * 
     * @param fileName The desired file name (without path)
     * @param text The text content for the PDF
     * @return The S3 key where the file was uploaded, or null if failed
     */
    private String generateSamplePdf(String fileName, String text) {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }

        // Add timestamp to ensure unique file names
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String s3Key = pdfPrefix + timestamp + "_" + fileName;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Generate PDF in memory instead of writing to local file system
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();

            // Upload to S3 for durable storage
            byte[] pdfBytes = baos.toByteArray();
            boolean uploadSuccess = s3Service.uploadFile(s3Key, pdfBytes, "application/pdf", bucketName);

            if (uploadSuccess) {
                log.info("PDF successfully uploaded to S3: s3://{}/{}", bucketName, s3Key);
                return s3Key;
            } else {
                log.error("Failed to upload PDF to S3: {}", s3Key);
                return null;
            }

        } catch (DocumentException e) {
            log.error("Error generating PDF document: {}", e.getMessage(), e);
            return null;
        } catch (IOException e) {
            log.error("Error closing ByteArrayOutputStream: {}", e.getMessage(), e);
            return null;
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
            String s3Key = generateSamplePdf(pdf.getName(), pdf.getContent());
            
            if (s3Key != null) {
                // Store the S3 key in the PDF entity for future reference
                pdf.setName(s3Key);
                pdfService.savePdf(pdf);
                model.addAttribute("s3Key", s3Key);
                model.addAttribute("bucketName", bucketName);
                log.info("PDF generated and saved successfully. S3 location: s3://{}/{}", bucketName, s3Key);
                return "pdf/success";
            } else {
                log.error("Failed to generate and upload PDF");
                model.addAttribute("error", "Failed to generate PDF. Please try again.");
                return "pdf/generator";
            }
        }
    }

}
