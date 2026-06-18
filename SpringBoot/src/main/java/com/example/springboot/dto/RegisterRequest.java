package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "姓名不能为空")
    private String customerName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "手机号须为11位数字")
    private String phone;

    @NotBlank(message = "收货地址不能为空")
    private String address;

    @NotBlank(message = "密码不能为空")
    private String password;
}
