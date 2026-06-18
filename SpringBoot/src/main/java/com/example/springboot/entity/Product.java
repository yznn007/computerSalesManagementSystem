package com.example.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Integer productId;
    private String brand;
    private String model;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String partType; // DIY配件分类时由 LEFT JOIN Spare_Part_Detail 填充
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
