package com.example.springboot.dto;

import lombok.Data;

/**
 * 更新个人信息请求 DTO，客户与销售员通用。
 * 根据当前登录角色，分别更新对应的客户字段或销售员字段。
 */
@Data
public class UpdateProfileRequest {
    // 客户字段
    private String customerName; // 客户姓名
    private String phone; // 手机号
    private String address; // 收货地址
    // 销售员字段
    private String staffName; // 销售员姓名
    private String username; // 登录用户名
}
