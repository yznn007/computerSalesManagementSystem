package com.example.springboot.init;

import com.example.springboot.mapper.CustomerMapper;
import com.example.springboot.mapper.StaffMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private static final String CUSTOMER_SEED = "__SEED_123456__";
    private static final String STAFF_SEED = "__SEED_ADMIN123__";

    private final CustomerMapper customerMapper;
    private final StaffMapper staffMapper;
    private final PasswordEncoder encoder;

    public DataInitializer(CustomerMapper customerMapper, StaffMapper staffMapper, PasswordEncoder encoder) {
        this.customerMapper = customerMapper;
        this.staffMapper = staffMapper;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        // 将客户种子密码替换为 BCrypt("123456")
        customerMapper.replaceSeedPasswords(CUSTOMER_SEED, encoder.encode("123456"));

        // 若无销售员账号，插入默认 admin
        if (staffMapper.countAll() == 0) {
            com.example.springboot.entity.Staff s = new com.example.springboot.entity.Staff();
            s.setUsername("admin");
            s.setPasswordHash(encoder.encode("admin123"));
            s.setStaffName("系统管理员");
            staffMapper.insert(s);
        } else {
            staffMapper.replaceSeedPasswords(STAFF_SEED, encoder.encode("admin123"));
        }
    }
}
