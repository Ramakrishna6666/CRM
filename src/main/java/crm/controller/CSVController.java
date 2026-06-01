package crm.controller;

import crm.entity.Customer;
import crm.service.CustomerService;
import crm.utils.WriteCsvToResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class CSVController {

    private CustomerService customerService;

    public CSVController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping(value = "/customers", produces = "text/csv")
    public void findCustomers(HttpServletResponse httpServletResponse) throws IOException {
        List<Customer> customers = (List<Customer>) customerService.listAllCustomers();
        WriteCsvToResponse.writeCustomers(httpServletResponse.getWriter(), customers);
    }

    @GetMapping(value = "/customers/{id}", produces = "text/csv")
    public void findCustomer(@PathVariable Long id, HttpServletResponse httpServletResponse) throws IOException {
        Customer customer = customerService.showCustomer(id);
        WriteCsvToResponse.writeCustomer(httpServletResponse.getWriter(), customer);
    }

}
