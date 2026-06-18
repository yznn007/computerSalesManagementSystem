package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Staff {
    private Integer staffId;
    private String username;
    @JsonIgnore
    private String passwordHash;
    private String staffName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
