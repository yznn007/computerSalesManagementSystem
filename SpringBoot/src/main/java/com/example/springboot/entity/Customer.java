package com.example.springboot.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Customer {
    private Integer customerId;
    private String customerName;
    private String phone;
    private String address;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
