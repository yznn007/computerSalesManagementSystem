package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增/编辑商品请求 DTO。
 * 必填字段为 brand/model/price/stock/category，其余按商品分类选填对应详情。
 */
@Data
public class ProductUpsertRequest {
    @NotBlank(message = "品牌不能为空")
    private String brand; // 品牌

    @NotBlank(message = "型号不能为空")
    private String model; // 型号

    @NotNull(message = "售价不能为空")
    private BigDecimal price; // 售价

    @NotNull(message = "库存不能为空")
    private Integer stock; // 库存数量

    @NotBlank(message = "分类不能为空")
    private String category; // 分类（笔记本/台式机整机/DIY配件）

    // 笔记本详情（category=笔记本时填）
    private String screenSize; // 屏幕尺寸
    private String cpuModel; // CPU 型号
    private String gpuModel; // GPU 型号
    private String weight; // 重量

    // 台式机整机详情（category=台式机整机时填）
    private String formFactor; // 机箱规格
    private String cpuDesc; // CPU 描述
    private String gpuDesc; // GPU 描述
    private String ramDesc; // 内存描述
    private String storageDesc; // 存储描述

    // DIY配件详情（category=DIY配件时填）
    private String partType; // 配件类型
    private String specification; // 规格参数
}
