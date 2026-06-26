package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 销售员实体，对应 Staff 表。
 * 销售员（staff）可管理商品、客户、订单；密码哈希不输出到 JSON。
 */
@Data
public class Staff {
    private Integer staffId; // 销售员主键
    private String username; // 登录用户名
    @JsonIgnore
    private String passwordHash; // BCrypt 密码哈希，序列化时忽略
    private String staffName; // 销售员姓名
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
