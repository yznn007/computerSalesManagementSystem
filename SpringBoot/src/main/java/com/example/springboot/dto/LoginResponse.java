package com.example.springboot.dto;

/**
 * 登录响应 DTO，返回 JWT 令牌、角色、用户 ID 与姓名。
 */
public record LoginResponse(String token, String role, Long id, String name) {}
