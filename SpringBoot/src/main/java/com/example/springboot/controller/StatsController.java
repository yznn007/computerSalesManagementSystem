package com.example.springboot.controller;

import com.example.springboot.security.AuthContext;
import com.example.springboot.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 销售看板统计接口（/api/stats），仅限销售员。
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** 看板总览：客户/订单/销售额/商品四维统计（仅销售员） */
    @GetMapping("/overview")
    public Map<String, Object> overview() {
        AuthContext.requireStaff();
        return statsService.overview();
    }
}