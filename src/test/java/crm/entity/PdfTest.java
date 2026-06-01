package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdfTest {

    private Pdf pdf;

    @BeforeEach
    void setUp() {
        pdf = new Pdf();
    }

    @Test
    void testDefaultConstructor() {
        Pdf p = new Pdf();
        assertNotNull(p);
    }

    @Test
    void testAllArgsConstructor() {
        Pdf p = new Pdf(1L, "test.pdf", "PDF content here");
        assertNotNull(p);
        assertEquals(1L, p.getId());
        assertEquals("test.pdf", p.getName());
        assertEquals("PDF content here", p.getContent());
    }

    @Test
    void testBuilderPattern() {
        Pdf p = Pdf.builder()
                .id(2L)
                .name("report.pdf")
                .content("Report content")
                .build();
        assertNotNull(p);
        assertEquals(2L, p.getId());
        assertEquals("report.pdf", p.getName());
        assertEquals("Report content", p.getContent());
    }

    @Test
    void testSetAndGetId() {
        pdf.setId(5L);
        assertEquals(5L, pdf.getId());
    }

    @Test
    void testSetAndGetName() {
        pdf.setName("document.pdf");
        assertEquals("document.pdf", pdf.getName());
    }

    @Test
    void testSetAndGetContent() {
        pdf.setContent("Some PDF content");
        assertEquals("Some PDF content", pdf.getContent());
    }

    @Test
    void testNullName() {
        pdf.setName(null);
        assertNull(pdf.getName());
    }

    @Test
    void testNullContent() {
        pdf.setContent(null);
        assertNull(pdf.getContent());
    }

    @Test
    void testEqualsAndHashCode() {
        Pdf p1 = Pdf.builder().id(1L).name("doc.pdf").build();
        Pdf p2 = Pdf.builder().id(1L).name("doc.pdf").build();
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void testNotEquals() {
        Pdf p1 = Pdf.builder().id(1L).name("doc1.pdf").build();
        Pdf p2 = Pdf.builder().id(2L).name("doc2.pdf").build();
        assertNotEquals(p1, p2);
    }

    @Test
    void testToString() {
        pdf.setId(1L);
        pdf.setName("test.pdf");
        String str = pdf.toString();
        assertNotNull(str);
        assertTrue(str.contains("test.pdf"));
    }

    @Test
    void testNameWithoutExtension() {
        pdf.setName("myDocument");
        assertEquals("myDocument", pdf.getName());
    }
}
