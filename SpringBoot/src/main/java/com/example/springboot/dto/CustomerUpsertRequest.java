package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 新增/编辑客户请求 DTO，供销售员使用。
 * 新增时 password 可选（留空默认 123456），编辑时不传。
 */
@Data
public class CustomerUpsertRequest {
    @NotBlank(message = "姓名不能为空")
    private String customerName; // 客户姓名

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^\\d{11}$", message = "手机号须为11位数字")
    private String phone; // 手机号，须为 11 位数字

    @NotBlank(message = "收货地址不能为空")
    private String address; // 收货地址

    // 新增客户时设置密码；留空则默认 123456。编辑时不传。
    private String password; // 密码（新增时可选，默认 123456；编辑时不传）
}
