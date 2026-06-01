package crm.viewResolver;

import crm.view.ExcelView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.View;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ExcelViewResolverTest {

    private ExcelViewResolver excelViewResolver;

    @BeforeEach
    void setUp() {
        excelViewResolver = new ExcelViewResolver();
    }

    @Test
    void testResolveViewName() throws Exception {
        View view = excelViewResolver.resolveViewName("anyView", Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_ReturnsExcelView() throws Exception {
        View view = excelViewResolver.resolveViewName("download", Locale.ENGLISH);
        assertInstanceOf(ExcelView.class, view);
    }

    @Test
    void testResolveViewName_WithNullViewName() throws Exception {
        View view = excelViewResolver.resolveViewName(null, Locale.ENGLISH);
        assertNotNull(view);
    }

    @Test
    void testResolveViewName_WithDifferentLocale() throws Exception {
        View view = excelViewResolver.resolveViewName("test", Locale.GERMAN);
        assertNotNull(view);
        assertInstanceOf(ExcelView.class, view);
    }

    @Test
    void testResolveViewName_AlwaysReturnsExcelView() throws Exception {
        View view1 = excelViewResolver.resolveViewName("view1", Locale.ENGLISH);
        View view2 = excelViewResolver.resolveViewName("view2", Locale.ENGLISH);
        assertInstanceOf(ExcelView.class, view1);
        assertInstanceOf(ExcelView.class, view2);
    }
}
