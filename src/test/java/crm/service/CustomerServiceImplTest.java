package crm.service;

import crm.entity.Customer;
import crm.entity.Category;
import crm.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("TestCo");
        customer.setEmail("test@test.com");
        customer.setPhone(123456789);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setCity("NYC");
        customer.setAddress("123 Main St");
        customer.setEnabled(1);
    }

    @Test
    void testGetMaxId() {
        when(customerRepository.getMaxId()).thenReturn(5L);
        Long maxId = customerService.getMaxId();
        assertEquals(5L, maxId);
        verify(customerRepository).getMaxId();
    }

    @Test
    void testListAllCustomers() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findAll()).thenReturn(customers);
        Iterable<Customer> result = customerService.listAllCustomers();
        assertNotNull(result);
        verify(customerRepository).findAll();
    }

    @Test
    void testShowCustomer_Found() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        Customer result = customerService.showCustomer(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(customerRepository).findById(1L);
    }

    @Test
    void testShowCustomer_NotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        Customer result = customerService.showCustomer(99L);
        assertNull(result);
        verify(customerRepository).findById(99L);
    }

    @Test
    void testFindAllByEnabledTrue() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findAllByEnabled(1)).thenReturn(customers);
        Iterable<Customer> result = customerService.findAllByEnabledTrue();
        assertNotNull(result);
        verify(customerRepository).findAllByEnabled(1);
    }

    @Test
    void testFindAllByEnabledFalse() {
        when(customerRepository.findAllByEnabled(0)).thenReturn(Collections.emptyList());
        Iterable<Customer> result = customerService.findAllByEnabledFalse();
        assertNotNull(result);
        verify(customerRepository).findAllByEnabled(0);
    }

    @Test
    void testFindOneByEnabledTrueAndName() {
        when(customerRepository.findOneByEnabledAndName(1, "TestCo")).thenReturn(customer);
        Customer result = customerService.findOneByEnabledTrueAndName("TestCo");
        assertNotNull(result);
        assertEquals("TestCo", result.getName());
        verify(customerRepository).findOneByEnabledAndName(1, "TestCo");
    }

    @Test
    void testFindOneByEnabledFalseAndName() {
        when(customerRepository.findOneByEnabledAndName(0, "TestCo")).thenReturn(null);
        Customer result = customerService.findOneByEnabledFalseAndName("TestCo");
        assertNull(result);
        verify(customerRepository).findOneByEnabledAndName(0, "TestCo");
    }

    @Test
    void testFindOneByName() {
        when(customerRepository.findOneByName("TestCo")).thenReturn(customer);
        Customer result = customerService.findOneByName("TestCo");
        assertNotNull(result);
        verify(customerRepository).findOneByName("TestCo");
    }

    @Test
    void testFindByEnabledTrueAndEmail() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndEmail(1, "test@test.com")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndEmail("test@test.com");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndEmail(1, "test@test.com");
    }

    @Test
    void testFindByEnabledFalseAndEmail() {
        when(customerRepository.findByEnabledAndEmail(0, "test@test.com")).thenReturn(Collections.emptyList());
        Iterable<Customer> result = customerService.findByEnabledFalseAndEmail("test@test.com");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndEmail(0, "test@test.com");
    }

    @Test
    void testFindByEmail() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEmail("test@test.com")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEmail("test@test.com");
        assertNotNull(result);
        verify(customerRepository).findByEmail("test@test.com");
    }

    @Test
    void testFindByEnabledTrueAndPhone() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndPhone(1, 123456789)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndPhone(123456789);
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndPhone(1, 123456789);
    }

    @Test
    void testFindByEnabledFalseAndPhone() {
        when(customerRepository.findByEnabledAndPhone(0, 123456789)).thenReturn(Collections.emptyList());
        Iterable<Customer> result = customerService.findByEnabledFalseAndPhone(123456789);
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndPhone(0, 123456789);
    }

    @Test
    void testFindByPhone() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByPhone(123456789)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByPhone(123456789);
        assertNotNull(result);
        verify(customerRepository).findByPhone(123456789);
    }

    @Test
    void testFindByEnabledTrueAndCategories() {
        Set<Category> categories = new HashSet<>();
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndCategories(1, categories)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndCategories(categories);
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCategories(1, categories);
    }

    @Test
    void testFindByEnabledFalseAndCategories() {
        Set<Category> categories = new HashSet<>();
        when(customerRepository.findByEnabledAndCategories(0, categories)).thenReturn(Collections.emptyList());
        Iterable<Customer> result = customerService.findByEnabledFalseAndCategories(categories);
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCategories(0, categories);
    }

    @Test
    void testFindByCategories() {
        Set<Category> categories = new HashSet<>();
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByCategories(categories)).thenReturn(customers);
        Iterable<Customer> result = customerService.findByCategories(categories);
        assertNotNull(result);
        verify(customerRepository).findByCategories(categories);
    }

    @Test
    void testFindByEnabledTrueAndFirstName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndFirstName(1, "John")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndFirstName("John");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstName(1, "John");
    }

    @Test
    void testFindByEnabledFalseAndFirstName() {
        when(customerRepository.findByEnabledAndFirstName(0, "John")).thenReturn(Collections.emptyList());
        Iterable<Customer> result = customerService.findByEnabledFalseAndFirstName("John");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstName(0, "John");
    }

    @Test
    void testFindByFirstName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByFirstName("John")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByFirstName("John");
        assertNotNull(result);
        verify(customerRepository).findByFirstName("John");
    }

    @Test
    void testFindByEnabledTrueAndLastName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndLastName(1, "Doe")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndLastName("Doe");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndLastName(1, "Doe");
    }

    @Test
    void testFindByEnabledFalseAndLastName() {
        when(customerRepository.findByEnabledAndLastName(0, "Doe")).thenReturn(Collections.emptyList());
        Iterable<Customer> result = customerService.findByEnabledFalseAndLastName("Doe");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndLastName(0, "Doe");
    }

    @Test
    void testFindByLastName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByLastName("Doe")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByLastName("Doe");
        assertNotNull(result);
        verify(customerRepository).findByLastName("Doe");
    }

    @Test
    void testFindByEnabledTrueAndFirstNameAndLastName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndFirstNameAndLastName(1, "John", "Doe")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndFirstNameAndLastName("John", "Doe");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstNameAndLastName(1, "John", "Doe");
    }

    @Test
    void testFindByEnabledFalseAndFirstNameAndLastName() {
        when(customerRepository.findByEnabledAndFirstNameAndLastName(0, "John", "Doe")).thenReturn(Collections.emptyList());
        Iterable<Customer> result = customerService.findByEnabledFalseAndFirstNameAndLastName("John", "Doe");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndFirstNameAndLastName(0, "John", "Doe");
    }

    @Test
    void testFindByFirstNameAndLastName() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByFirstNameAndLastName("John", "Doe")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByFirstNameAndLastName("John", "Doe");
        assertNotNull(result);
        verify(customerRepository).findByFirstNameAndLastName("John", "Doe");
    }

    @Test
    void testFindByEnabledTrueAndCity() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndCity(1, "NYC")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndCity("NYC");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCity(1, "NYC");
    }

    @Test
    void testFindByEnabledFalseAndCity() {
        when(customerRepository.findByEnabledAndCity(0, "NYC")).thenReturn(Collections.emptyList());
        Iterable<Customer> result = customerService.findByEnabledFalseAndCity("NYC");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCity(0, "NYC");
    }

    @Test
    void testFindByCity() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByCity("NYC")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByCity("NYC");
        assertNotNull(result);
        verify(customerRepository).findByCity("NYC");
    }

    @Test
    void testFindByEnabledTrueAndCityAndAddress() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByEnabledAndCityAndAddress(1, "NYC", "123 Main St")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByEnabledTrueAndCityAndAddress("NYC", "123 Main St");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCityAndAddress(1, "NYC", "123 Main St");
    }

    @Test
    void testFindByEnabledFalseAndCityAndAddress() {
        when(customerRepository.findByEnabledAndCityAndAddress(0, "NYC", "123 Main St")).thenReturn(Collections.emptyList());
        Iterable<Customer> result = customerService.findByEnabledFalseAndCityAndAddress("NYC", "123 Main St");
        assertNotNull(result);
        verify(customerRepository).findByEnabledAndCityAndAddress(0, "NYC", "123 Main St");
    }

    @Test
    void testFindByCityAndAddress() {
        List<Customer> customers = Arrays.asList(customer);
        when(customerRepository.findByCityAndAddress("NYC", "123 Main St")).thenReturn(customers);
        Iterable<Customer> result = customerService.findByCityAndAddress("NYC", "123 Main St");
        assertNotNull(result);
        verify(customerRepository).findByCityAndAddress("NYC", "123 Main St");
    }

    @Test
    void testSaveCustomer() {
        Customer newCustomer = new Customer();
        newCustomer.setName("NewCo");
        newCustomer.setEmail("new@test.com");
        customerService.saveCustomer(newCustomer);
        assertEquals(1, newCustomer.getEnabled());
        verify(customerRepository).save(newCustomer);
    }

    @Test
    void testSaveCustomer_SetsEnabledToOne() {
        Customer c = new Customer();
        c.setEnabled(0);
        customerService.saveCustomer(c);
        assertEquals(1, c.getEnabled());
    }
}
