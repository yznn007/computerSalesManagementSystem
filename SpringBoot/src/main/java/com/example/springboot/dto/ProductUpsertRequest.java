package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpsertRequest {
    @NotBlank(message = "品牌不能为空")
    private String brand;

    @NotBlank(message = "型号不能为空")
    private String model;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    @NotBlank(message = "分类不能为空")
    private String category;

    // 笔记本详情（category=笔记本时填）
    private String screenSize;
    private String cpuModel;
    private String gpuModel;
    private String weight;

    // 台式机整机详情（category=台式机整机时填）
    private String formFactor;
    private String cpuDesc;
    private String gpuDesc;
    private String ramDesc;
    private String storageDesc;

    // DIY配件详情（category=DIY配件时填）
    private String partType;
    private String specification;
}
