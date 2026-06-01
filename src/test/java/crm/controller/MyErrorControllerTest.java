package crm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MyErrorControllerTest {

    @InjectMocks
    private MyErrorController myErrorController;

    @Test
    void testHandleError() {
        String view = myErrorController.handleError();
        assertEquals("error", view);
    }

    @Test
    void testHandleError_ReturnsNonNull() {
        String view = myErrorController.handleError();
        assertNotNull(view);
    }

    @Test
    void testHandleError_ReturnsErrorString() {
        String view = myErrorController.handleError();
        assertEquals("error", view);
        assertFalse(view.isEmpty());
    }

    @Test
    void testControllerInstantiation() {
        MyErrorController controller = new MyErrorController();
        assertNotNull(controller);
    }
}
