package com.example.springboot.service;

import com.example.springboot.dto.LoginRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.RegisterRequest;
import com.example.springboot.dto.UpdateProfileRequest;
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
        String pwd = (req.getPassword() != null && !req.getPassword().isBlank())
                ? req.getPassword() : "123456";
        c.setPasswordHash(encoder.encode(pwd));
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

    /** 获取当前用户详细信息 */
    public Object getProfile() {
        AuthContext.CurrentUser u = AuthContext.require();
        if (AuthContext.ROLE_STAFF.equals(u.role())) {
            Staff s = staffMapper.findById(u.id().intValue());
            if (s == null) throw new com.example.springboot.common.BizException(404, "账号不存在");
            return s;
        }
        Customer c = customerMapper.findById(u.id().intValue());
        if (c == null) throw new com.example.springboot.common.BizException(404, "账号不存在");
        return c;
    }

    /** 更新基本信息（客户改 name/phone/address，销售员改 name/username） */
    public Object updateProfile(UpdateProfileRequest req, AuthContext.CurrentUser u) {
        if (AuthContext.ROLE_STAFF.equals(u.role())) {
            Staff s = staffMapper.findById(u.id().intValue());
            if (s == null) throw new com.example.springboot.common.BizException(404, "账号不存在");
            if (req.getUsername() != null && !req.getUsername().isBlank()
                    && !req.getUsername().equals(s.getUsername())) {
                if (staffMapper.findByUsername(req.getUsername()) != null) {
                    throw new com.example.springboot.common.BizException("用户名已存在");
                }
                s.setUsername(req.getUsername());
            }
            if (req.getStaffName() != null && !req.getStaffName().isBlank()) {
                s.setStaffName(req.getStaffName());
            }
            staffMapper.update(s);
            return s;
        }
        Customer c = customerMapper.findById(u.id().intValue());
        if (c == null) throw new com.example.springboot.common.BizException(404, "账号不存在");
        if (req.getCustomerName() != null && !req.getCustomerName().isBlank()) {
            c.setCustomerName(req.getCustomerName());
        }
        if (req.getPhone() != null && !req.getPhone().isBlank()
                && !req.getPhone().equals(c.getPhone())) {
            if (customerMapper.findByPhone(req.getPhone()) != null) {
                throw new com.example.springboot.common.BizException("手机号已存在");
            }
            c.setPhone(req.getPhone());
        }
        if (req.getAddress() != null && !req.getAddress().isBlank()) {
            c.setAddress(req.getAddress());
        }
        customerMapper.update(c);
        return c;
    }

    /** 修改密码 */
    public void changePassword(String oldPassword, String newPassword, AuthContext.CurrentUser u) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new com.example.springboot.common.BizException("新密码不能为空");
        }
        if (AuthContext.ROLE_STAFF.equals(u.role())) {
            Staff s = staffMapper.findById(u.id().intValue());
            if (s == null || !encoder.matches(oldPassword, s.getPasswordHash())) {
                throw new com.example.springboot.common.BizException("原密码错误");
            }
            staffMapper.updatePasswordHashById(s.getStaffId(), encoder.encode(newPassword));
            return;
        }
        Customer c = customerMapper.findById(u.id().intValue());
        if (c == null || !encoder.matches(oldPassword, c.getPasswordHash())) {
            throw new com.example.springboot.common.BizException("原密码错误");
        }
        customerMapper.updatePasswordHashById(c.getCustomerId(), encoder.encode(newPassword));
    }
}
