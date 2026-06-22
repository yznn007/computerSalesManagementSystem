package com.example.springboot.dto;

public record LoginResponse(String token, String role, Long id, String name) {}
