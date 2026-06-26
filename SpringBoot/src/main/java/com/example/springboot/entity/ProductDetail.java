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
    private Integer productId; // 商品主键
    private String brand; // 品牌
    private String model; // 型号
    private BigDecimal price; // 售价
    private Integer stock; // 库存
    private String category; // 分类

    // 笔记本详情
    private String screenSize; // 屏幕尺寸
    private String cpuModel; // CPU 型号
    private String gpuModel; // GPU 型号
    private String weight; // 重量

    // 台式机整机详情
    private String formFactor; // 机箱规格
    private String cpuDesc; // CPU 描述
    private String gpuDesc; // GPU 描述
    private String ramDesc; // 内存描述
    private String storageDesc; // 存储描述

    // DIY配件详情
    private String partType; // 配件类型
    private String specification; // 规格参数

    // 台式机配件组成（仅台式机整机）
    private List<Map<String, Object>> composition; // 配件组成列表
}
