package com.example.springboot.common;

/**
 * 统一错误响应体。
 * 由 {@link GlobalExceptionHandler} 在抛出异常时构造并返回给前端，
 * 序列化后形如 {@code {"code":404,"message":"订单不存在"}}。
 *
 * @param code    业务/HTTP 状态码（与 BizException 的 code 对应）
 * @param message 面向前端展示的错误描述
 */
public record ApiError(int code, String message) {}
