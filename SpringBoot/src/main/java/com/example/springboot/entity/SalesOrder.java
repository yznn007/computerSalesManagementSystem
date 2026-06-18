package com.example.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SalesOrder {
    private Integer orderId;
    private String orderNo;
    private Integer customerId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private BigDecimal paidAmount;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 联表查询附带
    private String customerName;
}
