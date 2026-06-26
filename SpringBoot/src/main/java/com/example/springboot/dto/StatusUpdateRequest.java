package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单状态变更请求 DTO。
 * action 为状态流转动作（pay/ship/cancel/return）；
 * paymentMethod 在 pay 时必填，cancelReason 在 cancel 时必填。
 */
@Data
public class StatusUpdateRequest {
    @NotBlank(message = "操作类型不能为空")
    private String action; // 操作类型（pay/ship/cancel/return）

    // pay 时必填
    private String paymentMethod; // 支付方式（pay 时必填）

    // cancel 时必填
    private String cancelReason; // 取消原因（cancel 时必填）
}
