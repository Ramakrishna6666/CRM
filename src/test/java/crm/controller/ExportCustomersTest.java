package crm.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExportCustomersTest {

    @InjectMocks
    private ExportCustomers exportCustomers;

    @Test
    void testInstantiation() {
        ExportCustomers ec = new ExportCustomers();
        assertNotNull(ec);
    }

    @Test
    void testClassExists() {
        assertNotNull(exportCustomers);
    }
}
