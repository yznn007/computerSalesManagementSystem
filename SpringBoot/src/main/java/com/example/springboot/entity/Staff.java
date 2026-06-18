package com.example.springboot.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Staff {
    private Integer staffId;
    private String username;
    private String passwordHash;
    private String staffName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
