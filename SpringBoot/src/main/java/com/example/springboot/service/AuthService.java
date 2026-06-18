package com.example.springboot.service;

import com.example.springboot.dto.LoginRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.RegisterRequest;
import com.example.springboot.entity.Customer;
import com.example.springboot.entity.Staff;
import com.example.springboot.mapper.CustomerMapper;
import com.example.springboot.mapper.StaffMapper;
import com.example.springboot.security.AuthContext;
import com.example.springboot.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final CustomerMapper customerMapper;
    private final StaffMapper staffMapper;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthService(CustomerMapper customerMapper, StaffMapper staffMapper,
                       PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.customerMapper = customerMapper;
        this.staffMapper = staffMapper;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(RegisterRequest req) {
        Customer exists = customerMapper.findByPhone(req.getPhone());
        if (exists != null) {
            throw new com.example.springboot.common.BizException("该手机号已注册");
        }
        Customer c = new Customer();
        c.setCustomerName(req.getCustomerName());
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        c.setPasswordHash(encoder.encode(req.getPassword()));
        customerMapper.insert(c);
    }

    public LoginResponse loginCustomer(LoginRequest req) {
        Customer c = customerMapper.findByPhone(req.getAccount());
        if (c == null || !encoder.matches(req.getPassword(), c.getPasswordHash())) {
            throw new com.example.springboot.common.BizException(401, "手机号或密码错误");
        }
        String token = jwtUtil.generate(c.getCustomerId().longValue(), AuthContext.ROLE_CUSTOMER, c.getCustomerName());
        return new LoginResponse(token, AuthContext.ROLE_CUSTOMER, c.getCustomerId().longValue(), c.getCustomerName());
    }

    public LoginResponse loginStaff(LoginRequest req) {
        Staff s = staffMapper.findByUsername(req.getAccount());
        if (s == null || !encoder.matches(req.getPassword(), s.getPasswordHash())) {
            throw new com.example.springboot.common.BizException(401, "用户名或密码错误");
        }
        String token = jwtUtil.generate(s.getStaffId().longValue(), AuthContext.ROLE_STAFF, s.getStaffName());
        return new LoginResponse(token, AuthContext.ROLE_STAFF, s.getStaffId().longValue(), s.getStaffName());
    }
}
