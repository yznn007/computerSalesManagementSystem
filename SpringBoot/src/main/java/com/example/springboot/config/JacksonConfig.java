package com.example.springboot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 显式定义 ObjectMapper：
 * - SnakeCase 输出下划线 key（匹配前端 product_id/order_no 等）
 * - 注册 JavaTimeModule 处理 LocalDateTime
 * Spring Boot 4 的 webmvc starter 不再自动配置 ObjectMapper，故手动定义。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE); // 驼峰字段 -> 下划线 key
        mapper.registerModule(new JavaTimeModule());                          // 支持 LocalDateTime 等 java.time 类型
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);       // 日期输出 ISO 字符串而非时间戳数字
        return mapper;
    }
}
