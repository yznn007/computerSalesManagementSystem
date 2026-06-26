package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录请求 DTO，客户用手机号、销售员用用户名登录，二者均不可为空。
 */
public record LoginRequest(
        @NotBlank(message = "账号不能为空") String account, // 客户为手机号，销售员为用户名
        @NotBlank(message = "密码不能为空") String password // 明文密码
) {}
