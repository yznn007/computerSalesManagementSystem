package com.example.springboot.controller;

import com.example.springboot.security.AuthContext;
import com.example.springboot.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        AuthContext.requireStaff();
        return statsService.overview();
    }
}