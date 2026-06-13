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
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Cloud-native PDF controller that stores generated PDFs in Amazon S3
 * instead of local file system for durability and scalability.
 */
@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;
    private S3Client s3Client;

    @Value("${aws.s3.bucket.name:crm-data-bucket}")
    private String bucketName;

    @Value("${aws.s3.pdf.prefix:pdfs/}")
    private String pdfPrefix;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
        // Initialize S3 client with default credentials provider (uses IAM roles in cloud)
        this.s3Client = S3Client.builder()
                .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Generate PDF and upload to S3 instead of writing to local file system
     * @param fileName Name of the PDF file
     * @param text Content to include in the PDF
     * @return S3 key of the uploaded PDF
     */
    private String generateAndUploadPdfToS3(String fileName, String text) throws DocumentException, IOException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }

        // Generate PDF in memory using ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();

            // Upload to S3
            String s3Key = pdfPrefix + fileName;
            byte[] pdfBytes = outputStream.toByteArray();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(pdfBytes));
            
            log.info("PDF successfully uploaded to S3: s3://{}/{}", bucketName, s3Key);
            return s3Key;
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
                pdf.setS3Key(s3Key); // Store S3 key reference instead of local file path
                pdfService.savePdf(pdf);
                model.addAttribute("s3Key", s3Key);
                log.info("PDF generated and saved successfully: {}", s3Key);
            } catch (DocumentException e) {
                log.error("Error generating PDF document", e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (IOException e) {
                log.error("Error uploading PDF to S3", e);
                model.addAttribute("error", "Failed to upload PDF to cloud storage");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }

}
