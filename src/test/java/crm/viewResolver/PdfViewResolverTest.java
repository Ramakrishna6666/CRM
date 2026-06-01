package crm.viewResolver;

import crm.view.PdfView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class PdfViewResolverTest {

    private PdfViewResolver pdfViewResolver;

    @BeforeEach
    void setUp() {
        pdfViewResolver = new PdfViewResolver();
    }

    @Test
    void testResolveViewName() throws Exception {
        View view = pdfViewResolver.resolveViewName("anyView", Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_ReturnsPdfView() throws Exception {
        View view = pdfViewResolver.resolveViewName("download", Locale.ENGLISH);
        assertInstanceOf(PdfView.class, view);
    }

    @Test
    void testResolveViewName_WithNullViewName() throws Exception {
        View view = pdfViewResolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_WithDifferentLocale() throws Exception {
        View view = pdfViewResolver.resolveViewName("test", Locale.FRENCH);
        assertNotNull(view);
        assertInstanceOf(PdfView.class, view);
    }

    @Test
    void testResolveViewName_AlwaysReturnsPdfView() throws Exception {
        View view1 = pdfViewResolver.resolveViewName("view1", Locale.ENGLISH);
        View view2 = pdfViewResolver.resolveViewName("view2", Locale.ENGLISH);
        assertInstanceOf(PdfView.class, view1);
        assertInstanceOf(PdfView.class, view2);
    }
}
