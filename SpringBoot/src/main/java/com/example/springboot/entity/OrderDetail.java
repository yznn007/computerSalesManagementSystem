package com.example.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体，对应 Order_Detail 表。
 * 记录订单中每件商品的下单数量与单价快照（下单时锁定价格）。
 * brand/model/category 为联表查询时附带的商品冗余信息。
 */
@Data
public class OrderDetail {
    private Integer detailId; // 明细主键
    private Integer orderId; // 所属订单 ID
    private Integer productId; // 商品 ID
    private Integer quantity; // 购买数量
    private BigDecimal unitPrice; // 下单时的单价快照
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    // 联表查询附带
    private String brand; // 品牌（冗余）
    private String model; // 型号（冗余）
    private String category; // 分类（冗余）
}
