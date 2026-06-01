package crm.service;

import crm.entity.Pdf;
import crm.repository.PdfRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PdfServiceImplTest {

    @Mock
    private PdfRepository pdfRepository;

    @InjectMocks
    private PdfServiceImpl pdfService;

    private Pdf pdf;

    @BeforeEach
    void setUp() {
        pdf = new Pdf();
        pdf.setId(1L);
        pdf.setName("test.pdf");
        pdf.setContent("PDF content");
    }

    @Test
    void testFindByName() {
        when(pdfRepository.findByName("test.pdf")).thenReturn(pdf);
        Pdf result = pdfService.findByName("test.pdf");
        assertNotNull(result);
        assertEquals("test.pdf", result.getName());
        verify(pdfRepository).findByName("test.pdf");
    }

    @Test
    void testFindByName_NotFound() {
        when(pdfRepository.findByName("unknown.pdf")).thenReturn(null);
        Pdf result = pdfService.findByName("unknown.pdf");
        assertNull(result);
        verify(pdfRepository).findByName("unknown.pdf");
    }

    @Test
    void testSavePdf() {
        pdfService.savePdf(pdf);
        verify(pdfRepository).save(pdf);
    }

    @Test
    void testSavePdf_NewPdf() {
        Pdf newPdf = new Pdf();
        newPdf.setName("new.pdf");
        newPdf.setContent("New content");
        pdfService.savePdf(newPdf);
        verify(pdfRepository).save(newPdf);
    }

    @Test
    void testFindByName_EmptyString() {
        when(pdfRepository.findByName("")).thenReturn(null);
        Pdf result = pdfService.findByName("");
        assertNull(result);
        verify(pdfRepository).findByName("");
    }

    @Test
    void testSavePdf_WithNullContent() {
        pdf.setContent(null);
        pdfService.savePdf(pdf);
        verify(pdfRepository).save(pdf);
    }

    @Test
    void testFindByName_ReturnsCorrectPdf() {
        Pdf anotherPdf = new Pdf();
        anotherPdf.setId(2L);
        anotherPdf.setName("report.pdf");
        when(pdfRepository.findByName("report.pdf")).thenReturn(anotherPdf);
        Pdf result = pdfService.findByName("report.pdf");
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("report.pdf", result.getName());
    }
}
