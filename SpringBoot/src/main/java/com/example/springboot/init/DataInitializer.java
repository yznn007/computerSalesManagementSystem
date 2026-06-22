package com.example.springboot.init;

import com.example.springboot.entity.Staff;
import com.example.springboot.mapper.CustomerMapper;
import com.example.springboot.mapper.StaffMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiConsumer;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    // 自描述占位符格式：__SEED_<明文>__，由本类启动时提取明文并替换为 BCrypt 哈希
    private static final String SEED_PREFIX = "__SEED_";
    private static final String SEED_SUFFIX = "__";

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
        // 客户：把所有 __SEED_<明文>__ 占位密码替换为对应 BCrypt 哈希
        encodeSeeds(customerMapper.findSeedPasswords(), customerMapper::replaceSeedPasswords);

        // 销售员：若无账号则插入默认 admin（密码 admin），否则替换种子占位密码
        if (staffMapper.countAll() == 0) {
            Staff s = new Staff();
            s.setUsername("admin");
            s.setPasswordHash(encoder.encode("admin"));
            s.setStaffName("山田小姐");
            staffMapper.insert(s);
        } else {
            encodeSeeds(staffMapper.findSeedPasswords(), staffMapper::replaceSeedPasswords);
        }
    }

    // 遍历种子占位符，提取明文 encode 后回写
    private void encodeSeeds(List<String> seeds, BiConsumer<String, String> replacer) {
        for (String seed : seeds) {
            if (seed == null || !seed.startsWith(SEED_PREFIX) || !seed.endsWith(SEED_SUFFIX)) {
                continue;
            }
            String plain = seed.substring(SEED_PREFIX.length(), seed.length() - SEED_SUFFIX.length());
            if (!plain.isEmpty()) {
                replacer.accept(seed, encoder.encode(plain));
            }
        }
    }
}
