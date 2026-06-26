package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 客户实体，对应 Customer 表，存储注册客户的个人信息与登录凭证。
 * 密码哈希通过 @JsonIgnore 排除在 JSON 序列化之外。
 */
@Data
public class Customer {
    private Integer customerId; // 客户主键
    private String customerName; // 客户姓名
    private String phone; // 手机号（登录账号）
    private String address; // 收货地址
    @JsonIgnore
    private String passwordHash; // BCrypt 密码哈希，序列化时忽略
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
