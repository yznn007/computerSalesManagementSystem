package com.example.springboot.controller;

import com.example.springboot.dto.CustomerUpsertRequest;
import com.example.springboot.entity.Customer;
import com.example.springboot.security.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final com.example.springboot.service.CustomerService customerService;

    public CustomerController(com.example.springboot.service.CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public List<Customer> list() {
        AuthContext.requireStaff();
        return customerService.list();
    }

    @GetMapping("/{id}")
    public Customer get(@PathVariable Integer id) {
        AuthContext.requireStaff();
        return customerService.get(id);
    }

    @PostMapping
    public Customer create(@Valid @RequestBody CustomerUpsertRequest req) {
        AuthContext.requireStaff();
        return customerService.create(req);
    }

    @PutMapping("/{id}")
    public Customer update(@PathVariable Integer id, @Valid @RequestBody CustomerUpsertRequest req) {
        AuthContext.requireStaff();
        return customerService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        AuthContext.requireStaff();
        customerService.delete(id);
    }
}
