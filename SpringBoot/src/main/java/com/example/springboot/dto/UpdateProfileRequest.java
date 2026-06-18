package com.example.springboot.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    // 客户字段
    private String customerName;
    private String phone;
    private String address;
    // 销售员字段
    private String staffName;
    private String username;
}
