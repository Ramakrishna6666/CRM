package crm.viewResolver;

import crm.view.CsvView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class CsvViewResolverTest {

    private CsvViewResolver csvViewResolver;

    @BeforeEach
    void setUp() {
        csvViewResolver = new CsvViewResolver();
    }

    @Test
    void testResolveViewName() throws Exception {
        View view = csvViewResolver.resolveViewName("anyView", Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_ReturnsCsvView() throws Exception {
        View view = csvViewResolver.resolveViewName("download", Locale.ENGLISH);
        assertInstanceOf(CsvView.class, view);
    }

    @Test
    void testResolveViewName_WithNullViewName() throws Exception {
        View view = csvViewResolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_WithDifferentLocale() throws Exception {
        View view = csvViewResolver.resolveViewName("test", Locale.ITALIAN);
        assertNotNull(view);
        assertInstanceOf(CsvView.class, view);
    }

    @Test
    void testResolveViewName_AlwaysReturnsCsvView() throws Exception {
        View view1 = csvViewResolver.resolveViewName("view1", Locale.ENGLISH);
        View view2 = csvViewResolver.resolveViewName("view2", Locale.ENGLISH);
        assertInstanceOf(CsvView.class, view1);
        assertInstanceOf(CsvView.class, view2);
    }
}
