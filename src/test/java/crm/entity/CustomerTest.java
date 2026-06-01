package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
    }

    @Test
    void testDefaultConstructor() {
        Customer c = new Customer();
        assertNotNull(c);
    }

    @Test
    void testAllArgsConstructor() {
        Set<Category> categories = new HashSet<>();
        Customer c = new Customer(1L, "TestCo", "test@test.com", 123456789,
                categories, "John", "Doe", "NYC", "123 Main St", 1);
        assertNotNull(c);
        assertEquals(1L, c.getId());
        assertEquals("TestCo", c.getName());
        assertEquals("test@test.com", c.getEmail());
        assertEquals(123456789, c.getPhone());
        assertEquals("John", c.getFirstName());
        assertEquals("Doe", c.getLastName());
        assertEquals("NYC", c.getCity());
        assertEquals("123 Main St", c.getAddress());
        assertEquals(1, c.getEnabled());
    }

    @Test
    void testBuilderPattern() {
        Customer c = Customer.builder()
                .id(2L)
                .name("BuilderCo")
                .email("builder@test.com")
                .phone(987654321)
                .firstName("Jane")
                .lastName("Smith")
                .city("LA")
                .address("456 Oak Ave")
                .enabled(1)
                .build();
        assertNotNull(c);
        assertEquals(2L, c.getId());
        assertEquals("BuilderCo", c.getName());
        assertEquals("builder@test.com", c.getEmail());
    }

    @Test
    void testSetAndGetId() {
        customer.setId(10L);
        assertEquals(10L, customer.getId());
    }

    @Test
    void testSetAndGetName() {
        customer.setName("AcmeCorp");
        assertEquals("AcmeCorp", customer.getName());
    }

    @Test
    void testSetAndGetEmail() {
        customer.setEmail("acme@corp.com");
        assertEquals("acme@corp.com", customer.getEmail());
    }

    @Test
    void testSetAndGetPhone() {
        customer.setPhone(555123456);
        assertEquals(555123456, customer.getPhone());
    }

    @Test
    void testSetAndGetFirstName() {
        customer.setFirstName("Alice");
        assertEquals("Alice", customer.getFirstName());
    }

    @Test
    void testSetAndGetLastName() {
        customer.setLastName("Wonder");
        assertEquals("Wonder", customer.getLastName());
    }

    @Test
    void testSetAndGetCity() {
        customer.setCity("Chicago");
        assertEquals("Chicago", customer.getCity());
    }

    @Test
    void testSetAndGetAddress() {
        customer.setAddress("789 Elm St");
        assertEquals("789 Elm St", customer.getAddress());
    }

    @Test
    void testSetAndGetEnabled() {
        customer.setEnabled(1);
        assertEquals(1, customer.getEnabled());
    }

    @Test
    void testSetAndGetEnabledFalse() {
        customer.setEnabled(0);
        assertEquals(0, customer.getEnabled());
    }

    @Test
    void testSetAndGetCategories() {
        Set<Category> categories = new HashSet<>();
        Category cat = new Category();
        cat.setId(1L);
        cat.setName("VIP");
        categories.add(cat);
        customer.setCategories(categories);
        assertNotNull(customer.getCategories());
        assertEquals(1, customer.getCategories().size());
    }

    @Test
    void testEqualsAndHashCode() {
        Customer c1 = Customer.builder().id(1L).name("Co1").email("c1@test.com").build();
        Customer c2 = Customer.builder().id(1L).name("Co1").email("c1@test.com").build();
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testNotEquals() {
        Customer c1 = Customer.builder().id(1L).name("Co1").email("c1@test.com").build();
        Customer c2 = Customer.builder().id(2L).name("Co2").email("c2@test.com").build();
        assertNotEquals(c1, c2);
    }

    @Test
    void testToString() {
        customer.setId(1L);
        customer.setName("TestCo");
        String str = customer.toString();
        assertNotNull(str);
        assertTrue(str.contains("TestCo"));
    }

    @Test
    void testNullName() {
        customer.setName(null);
        assertNull(customer.getName());
    }

    @Test
    void testNullEmail() {
        customer.setEmail(null);
        assertNull(customer.getEmail());
    }
}
