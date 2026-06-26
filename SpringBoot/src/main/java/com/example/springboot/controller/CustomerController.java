package com.example.springboot.controller;

import com.example.springboot.dto.CustomerUpsertRequest;
import com.example.springboot.entity.Customer;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理接口（/api/customers）。
 * 全部端点仅限销售员（requireStaff），客户自身资料维护走 /api/auth/me。
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /** 客户列表（仅销售员） */
    @GetMapping
    public List<Customer> list() {
        AuthContext.requireStaff();
        return customerService.list();
    }

    /** 查单个客户（仅销售员） */
    @GetMapping("/{id}")
    public Customer get(@PathVariable Integer id) {
        AuthContext.requireStaff();
        return customerService.get(id);
    }

    /** 新建客户（仅销售员） */
    @PostMapping
    public Customer create(@Valid @RequestBody CustomerUpsertRequest req) {
        AuthContext.requireStaff();
        return customerService.create(req);
    }

    /** 更新客户（仅销售员） */
    @PutMapping("/{id}")
    public Customer update(@PathVariable Integer id, @Valid @RequestBody CustomerUpsertRequest req) {
        AuthContext.requireStaff();
        return customerService.update(id, req);
    }

    /** 删除客户（仅销售员） */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        AuthContext.requireStaff();
        customerService.delete(id);
    }
}
