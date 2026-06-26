package com.example.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体，对应 Product 表。
 * 通过 category 区分笔记本/台式机整机/DIY配件，具体规格由子表补充。
 */
@Data
public class Product {
    private Integer productId; // 商品主键
    private String brand; // 品牌
    private String model; // 型号
    private BigDecimal price; // 售价
    private Integer stock; // 库存数量
    private String category; // 分类（笔记本/台式机整机/DIY配件）
    private String partType; // DIY配件分类时由 LEFT JOIN Spare_Part_Detail 填充
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
