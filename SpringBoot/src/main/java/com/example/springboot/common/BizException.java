package com.example.springboot.common;

/**
 * 业务异常。
 * 约定 {@code code} 取 400-599 时会被 {@link GlobalExceptionHandler} 直接当作
 * HTTP 状态码返回；非该区间则统一回落为 400。各 Service 通过抛出本异常来中断
 * 流程并向前端反馈业务错误（如库存不足、订单不存在、权限不足等）。
 */
public class BizException extends RuntimeException {
    /** 业务/HTTP 状态码 */
    private final int code;

    /** 不指定状态码时默认 400（请求参数/业务校验类错误） */
    public BizException(String message) {
        super(message);
        this.code = 400;
    }

    /** 指定状态码，如 401 未登录、403 无权限、404 资源不存在 */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
