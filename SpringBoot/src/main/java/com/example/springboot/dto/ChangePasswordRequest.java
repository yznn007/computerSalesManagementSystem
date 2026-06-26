package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码请求 DTO，包含原密码与新密码，二者均不可为空。
 */
public record ChangePasswordRequest(
        @NotBlank(message = "原密码不能为空") String oldPassword, // 原密码，用于验证身份
        @NotBlank(message = "新密码不能为空") String newPassword // 新密码
) {}
