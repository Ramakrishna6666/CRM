package crm.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContractTest {

    private Contract contract;
    private Customer customer;
    private User user;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("TestCustomer");

        Role role = new Role();
        role.setId(1);
        role.setName("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(role);

        contract = new Contract();
        contract.setId(1L);
        contract.setName("Contract-001");
        contract.setContent("Contract content");
        contract.setValue(new BigDecimal("5000.00"));
        contract.setBeginDate(LocalDate.of(2024, 1, 1));
        contract.setEndDate(LocalDate.of(2024, 12, 31));
        contract.setStatus(Status.PROPOSED);
        contract.setCustomer(customer);
        contract.setUser(user);
    }

    @Test
    void testDefaultConstructor() {
        Contract c = new Contract();
        assertNotNull(c);
    }

    @Test
    void testAllArgsConstructor() {
        Contract c = new Contract(1L, "C-001", "Content", new BigDecimal("1000"),
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31),
                Status.PROPOSED, customer, user);
        assertNotNull(c);
        assertEquals(1L, c.getId());
        assertEquals("C-001", c.getName());
        assertEquals("Content", c.getContent());
        assertEquals(new BigDecimal("1000"), c.getValue());
        assertEquals(Status.PROPOSED, c.getStatus());
    }

    @Test
    void testBuilderPattern() {
        Contract c = Contract.builder()
                .id(2L)
                .name("C-002")
                .content("Builder content")
                .value(new BigDecimal("2000"))
                .beginDate(LocalDate.of(2024, 3, 1))
                .endDate(LocalDate.of(2024, 9, 30))
                .status(Status.NEGOTIATED)
                .customer(customer)
                .user(user)
                .build();
        assertNotNull(c);
        assertEquals(2L, c.getId());
        assertEquals("C-002", c.getName());
        assertEquals(Status.NEGOTIATED, c.getStatus());
    }

    @Test
    void testSetAndGetId() {
        contract.setId(99L);
        assertEquals(99L, contract.getId());
    }

    @Test
    void testSetAndGetName() {
        contract.setName("NewContract");
        assertEquals("NewContract", contract.getName());
    }

    @Test
    void testSetAndGetContent() {
        contract.setContent("New content");
        assertEquals("New content", contract.getContent());
    }

    @Test
    void testSetAndGetValue() {
        contract.setValue(new BigDecimal("9999.99"));
        assertEquals(new BigDecimal("9999.99"), contract.getValue());
    }

    @Test
    void testSetAndGetBeginDate() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        contract.setBeginDate(date);
        assertEquals(date, contract.getBeginDate());
    }

    @Test
    void testSetAndGetEndDate() {
        LocalDate date = LocalDate.of(2025, 12, 31);
        contract.setEndDate(date);
        assertEquals(date, contract.getEndDate());
    }

    @Test
    void testSetAndGetStatusProposed() {
        contract.setStatus(Status.PROPOSED);
        assertEquals(Status.PROPOSED, contract.getStatus());
    }

    @Test
    void testSetAndGetStatusNegotiated() {
        contract.setStatus(Status.NEGOTIATED);
        assertEquals(Status.NEGOTIATED, contract.getStatus());
    }

    @Test
    void testSetAndGetStatusImplemented() {
        contract.setStatus(Status.IMPLEMENTED);
        assertEquals(Status.IMPLEMENTED, contract.getStatus());
    }

    @Test
    void testSetAndGetStatusDone() {
        contract.setStatus(Status.DONE);
        assertEquals(Status.DONE, contract.getStatus());
    }

    @Test
    void testSetAndGetCustomer() {
        Customer newCustomer = new Customer();
        newCustomer.setId(2L);
        newCustomer.setName("NewCustomer");
        contract.setCustomer(newCustomer);
        assertEquals(newCustomer, contract.getCustomer());
    }

    @Test
    void testSetAndGetUser() {
        User newUser = new User();
        newUser.setId(2L);
        newUser.setUsername("newuser");
        contract.setUser(newUser);
        assertEquals(newUser, contract.getUser());
    }

    @Test
    void testEqualsAndHashCode() {
        Contract c1 = Contract.builder().id(1L).name("C-001").build();
        Contract c2 = Contract.builder().id(1L).name("C-001").build();
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    void testNotEquals() {
        Contract c1 = Contract.builder().id(1L).name("C-001").build();
        Contract c2 = Contract.builder().id(2L).name("C-002").build();
        assertNotEquals(c1, c2);
    }

    @Test
    void testToString() {
        String str = contract.toString();
        assertNotNull(str);
        assertTrue(str.contains("Contract-001"));
    }

    @Test
    void testNullValue() {
        contract.setValue(null);
        assertNull(contract.getValue());
    }
}
