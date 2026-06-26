package com.example.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密器配置。
 * 提供全局 {@link PasswordEncoder} Bean，注册/改密时用于生成 BCrypt 哈希，
 * 登录时用于校验明文与哈希。BCrypt 自带随机盐，无需额外维护盐字段。
 */
@Configuration
public class PasswordEncoderConfig {
    /** BCrypt 单向加密：DataInitializer 替换种子明文、AuthService 登录校验均依赖它 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
