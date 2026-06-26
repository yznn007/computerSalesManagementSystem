package com.example.springboot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用入口。
 * {@code @MapperScan} 扫描 mapper 包注册 MyBatis 接口为 Bean，免去逐个 @Mapper 注解。
 */
@SpringBootApplication
@MapperScan("com.example.springboot.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
