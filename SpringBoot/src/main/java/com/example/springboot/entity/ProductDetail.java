package com.example.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商品详情联合视图（GET /products/{id} 返回）
 * 包含 Product 基础字段 + 三类详情之一（Jackson SnakeCase 输出 screen_size 等）
 */
@Data
public class ProductDetail {
    private Integer productId;
    private String brand;
    private String model;
    private BigDecimal price;
    private Integer stock;
    private String category;

    // 笔记本详情
    private String screenSize;
    private String cpuModel;
    private String gpuModel;
    private String weight;

    // 台式机整机详情
    private String formFactor;
    private String cpuDesc;
    private String gpuDesc;
    private String ramDesc;
    private String storageDesc;

    // DIY配件详情
    private String partType;
    private String specification;

    // 台式机配件组成（仅台式机整机）
    private List<Map<String, Object>> composition;
}
