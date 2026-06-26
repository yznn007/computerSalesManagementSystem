package com.example.springboot.service;

import com.example.springboot.common.BizException;
import com.example.springboot.dto.CustomerUpsertRequest;
import com.example.springboot.entity.Customer;
import com.example.springboot.mapper.CustomerMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 客户管理业务层（仅供销售员使用）。
 * 提供客户增删改查；新增/改手机号时保证手机号唯一，删除时拦截外键约束给出友好提示。
 */
@Service
public class CustomerService {

    /** 销售员代建客户时的默认初始密码 */
    private static final String DEFAULT_PASSWORD = "123456";

    private final CustomerMapper customerMapper;
    private final PasswordEncoder encoder;

    public CustomerService(CustomerMapper customerMapper, PasswordEncoder encoder) {
        this.customerMapper = customerMapper;
        this.encoder = encoder;
    }

    /** 客户列表 */
    public List<Customer> list() {
        return customerMapper.findAll();
    }

    /** 按 id 查客户，不存在抛 404 */
    public Customer get(Integer id) {
        Customer c = customerMapper.findById(id);
        if (c == null) {
            throw new BizException(404, "客户不存在");
        }
        return c;
    }

    /** 新建客户：手机号查重后插入，未指定密码用默认密码（BCrypt 加密存储） */
    public Customer create(CustomerUpsertRequest req) {
        if (customerMapper.findByPhone(req.getPhone()) != null) {
            throw new BizException("该手机号已存在");
        }
        Customer c = new Customer();
        c.setCustomerName(req.getCustomerName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        String pwd = (req.getPassword() != null && !req.getPassword().isBlank())
                ? req.getPassword() : DEFAULT_PASSWORD;
        c.setPasswordHash(encoder.encode(pwd));
        customerMapper.insert(c);
        return c;
    }

    /** 更新客户资料（不改密码）；手机号变更时再次查重 */
    public Customer update(Integer id, CustomerUpsertRequest req) {
        Customer c = get(id);
        // 手机号变更需检查唯一
        if (!c.getPhone().equals(req.getPhone())) {
            if (customerMapper.findByPhone(req.getPhone()) != null) {
                throw new BizException("该手机号已存在");
            }
        }
        c.setCustomerName(req.getCustomerName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        customerMapper.update(c);
        return c;
    }

    /** 删除客户：存在关联订单（外键约束）时拦截并给出友好提示 */
    public void delete(Integer id) {
        get(id);
        try {
            customerMapper.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new BizException("该客户存在关联订单，无法删除");
        }
    }
}
