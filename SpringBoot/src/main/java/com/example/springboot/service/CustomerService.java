package com.example.springboot.service;

import com.example.springboot.dto.CustomerUpsertRequest;
import com.example.springboot.entity.Customer;
import com.example.springboot.mapper.CustomerMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final CustomerMapper customerMapper;
    private final PasswordEncoder encoder;

    public CustomerService(CustomerMapper customerMapper, PasswordEncoder encoder) {
        this.customerMapper = customerMapper;
        this.encoder = encoder;
    }

    public List<Customer> list() {
        return customerMapper.findAll();
    }

    public Customer get(Integer id) {
        Customer c = customerMapper.findById(id);
        if (c == null) {
            throw new com.example.springboot.common.BizException(404, "客户不存在");
        }
        return c;
    }

    public Customer create(CustomerUpsertRequest req) {
        if (customerMapper.findByPhone(req.getPhone()) != null) {
            throw new com.example.springboot.common.BizException("该手机号已存在");
        }
        Customer c = new Customer();
        c.setCustomerName(req.getCustomerName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        c.setPasswordHash(encoder.encode(DEFAULT_PASSWORD));
        customerMapper.insert(c);
        return c;
    }

    public Customer update(Integer id, CustomerUpsertRequest req) {
        Customer c = get(id);
        // 手机号变更需检查唯一
        if (!c.getPhone().equals(req.getPhone())) {
            if (customerMapper.findByPhone(req.getPhone()) != null) {
                throw new com.example.springboot.common.BizException("该手机号已存在");
            }
        }
        c.setCustomerName(req.getCustomerName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        customerMapper.update(c);
        return c;
    }

    public void delete(Integer id) {
        get(id);
        try {
            customerMapper.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new com.example.springboot.common.BizException("该客户存在关联订单，无法删除");
        }
    }
}
