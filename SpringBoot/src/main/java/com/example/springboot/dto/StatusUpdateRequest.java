package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotBlank(message = "操作类型不能为空")
    private String action;

    // pay 时必填
    private String paymentMethod;

    // cancel 时必填
    private String cancelReason;
}
