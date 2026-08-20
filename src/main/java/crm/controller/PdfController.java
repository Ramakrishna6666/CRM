package crm.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import crm.entity.Pdf;
import crm.service.AzureBlobStorageService;
import crm.service.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;

@Controller
@Slf4j
public class PdfController {

    private PdfService pdfService;
    private AzureBlobStorageService azureBlobStorageService;

    public PdfController(PdfService pdfService, AzureBlobStorageService azureBlobStorageService) {
        this.pdfService = pdfService;
        this.azureBlobStorageService = azureBlobStorageService;
    }

    /**
     * Generate PDF and upload to Azure Blob Storage instead of local file system.
     * This ensures data durability and availability across container restarts and scaling events.
     *
     * @param fileName The name of the PDF file
     * @param text     The content to include in the PDF
     * @return The URL of the uploaded blob in Azure Blob Storage
     * @throws DocumentException if PDF generation fails
     */
    private String generateSamplePdf(String fileName, String text) throws DocumentException {
        if (!fileName.endsWith(".pdf")) {
            fileName += ".pdf";
        }
        
        // Generate PDF in memory using ByteArrayOutputStream instead of FileOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        
        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Paragraph paragraph = new Paragraph(text);
            document.add(paragraph);
            document.close();
            
            // Upload to Azure Blob Storage for persistent, cloud-native storage
            byte[] pdfBytes = outputStream.toByteArray();
            String blobUrl = azureBlobStorageService.uploadFile(fileName, pdfBytes);
            
            log.info("PDF generated and uploaded to Azure Blob Storage: {}", blobUrl);
            return blobUrl;
        } catch (Exception e) {
            log.error("Failed to generate or upload PDF: {}", e.getMessage(), e);
            throw new DocumentException("Failed to generate or upload PDF", e);
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                log.warn("Failed to close output stream: {}", e.getMessage());
            }
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
                // Generate PDF and upload to Azure Blob Storage
                String blobUrl = generateSamplePdf(pdf.getName(), pdf.getContent());
                
                // Save PDF metadata to database
                pdfService.savePdf(pdf);
                
                // Add blob URL to model for display
                model.addAttribute("blobUrl", blobUrl);
                
                log.info("PDF successfully generated and stored in Azure Blob Storage");
            } catch (DocumentException e) {
                log.error("Failed to generate PDF document: {}", e.getMessage(), e);
                model.addAttribute("error", "Failed to generate PDF document");
                return "pdf/generator";
            } catch (Exception e) {
                log.error("Failed to upload PDF to Azure Blob Storage: {}", e.getMessage(), e);
                model.addAttribute("error", "Failed to upload PDF to cloud storage");
                return "pdf/generator";
            }
            return "pdf/success";
        }
    }

}
