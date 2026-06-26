package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 客户注册请求 DTO。
 * 密码可选，留空则系统默认设置为 123456。
 */
@Data
public class RegisterRequest {
    @NotBlank(message = "姓名不能为空")
    private String customerName; // 客户姓名

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "手机号须为11位数字")
    private String phone; // 手机号（登录账号），须为 11 位数字

    @NotBlank(message = "收货地址不能为空")
    private String address; // 收货地址

    // 密码可选，留空则默认 123456
    private String password; // 明文密码（可选，默认 123456）
}
