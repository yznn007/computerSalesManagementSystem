package com.example.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售订单实体，对应 Sales_Order 表。
 * 状态机（status 存中文枚举，默认"待付款"）：
 * 待付款 --付款--> 已付款 --发货--> 已发货；待付款/已付款可"取消"为已取消，已发货可"退货"为已退货。
 * 对应控制器动作：pay/ship/cancel/return。
 */
@Data
public class SalesOrder {
    private Integer orderId; // 订单主键
    private String orderNo; // 订单编号（业务唯一标识）
    private Integer customerId; // 下单客户 ID
    private LocalDateTime orderDate; // 下单时间
    private BigDecimal totalAmount; // 订单总金额
    private String status; // 订单状态：待付款/已付款/已发货/已取消/已退货
    private String paymentMethod; // 支付方式
    private LocalDateTime paymentTime; // 付款时间
    private BigDecimal paidAmount; // 已付金额
    private String cancelReason; // 取消原因
    private LocalDateTime cancelTime; // 取消时间
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    // 联表查询附带
    private String customerName; // 客户姓名（冗余）
}
