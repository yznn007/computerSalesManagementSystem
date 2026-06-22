package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Customer {
    private Integer customerId;
    private String customerName;
    private String phone;
    private String address;
    @JsonIgnore
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
