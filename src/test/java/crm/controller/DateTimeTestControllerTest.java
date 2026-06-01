package crm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DateTimeTestControllerTest {

    @Mock
    private Model model;

    @InjectMocks
    private DateTimeTestController dateTimeTestController;

    @Test
    void testDateTimeTest() {
        String view = dateTimeTestController.dateTimeTest(model);
        assertEquals("date/test", view);
    }

    @Test
    void testDateTimeTest_AddsStandardDate() {
        dateTimeTestController.dateTimeTest(model);
        verify(model).addAttribute(eq("standardDate"), any());
    }

    @Test
    void testDateTimeTest_AddsLocalDateTime() {
        dateTimeTestController.dateTimeTest(model);
        verify(model).addAttribute(eq("localDateTime"), any());
    }

    @Test
    void testDateTimeTest_AddsLocalDate() {
        dateTimeTestController.dateTimeTest(model);
        verify(model).addAttribute(eq("localDate"), any());
    }

    @Test
    void testDateTimeTest_AddsTimestamp() {
        dateTimeTestController.dateTimeTest(model);
        verify(model).addAttribute(eq("timestamp"), any());
    }

    @Test
    void testDateTimeTest_ReturnsNonNull() {
        String view = dateTimeTestController.dateTimeTest(model);
        assertNotNull(view);
    }

    @Test
    void testDateTimeTest_AddsAllFourAttributes() {
        dateTimeTestController.dateTimeTest(model);
        verify(model, times(4)).addAttribute(anyString(), any());
    }
}
