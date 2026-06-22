package com.example.springboot.service;

import com.example.springboot.mapper.StatsMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final StatsMapper statsMapper;

    public StatsService(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 客户统计
        Map<String, Object> customers = new LinkedHashMap<>();
        long customerTotal = statsMapper.customerTotal();
        customers.put("total", customerTotal);
        customers.put("active", statsMapper.activeCustomerCount());
        long orderTotal = statsMapper.orderTotal();
        customers.put("avg_orders", customerTotal > 0 ? String.format("%.1f", (double) orderTotal / customerTotal) : "0.0");
        result.put("customers", customers);

        // 订单统计
        List<Map<String, Object>> statusItems = statsMapper.orderCountByStatus();
        for (Map<String, Object> item : statusItems) {
            item.put("count", ((Number) item.get("count")).intValue());
        }
        Map<String, Object> orders = new LinkedHashMap<>();
        orders.put("total", orderTotal);
        orders.put("by_status", statusItems);
        result.put("orders", orders);

        // 销售额统计
        Map<String, Object> sales = new LinkedHashMap<>();
        sales.put("paid", statsMapper.paidAmount());
        sales.put("pending", statsMapper.pendingAmount());
        sales.put("received", statsMapper.receivedAmount());
        result.put("sales", sales);

        // 商品统计
        List<Map<String, Object>> categoryItems = statsMapper.productCountByCategory();
        for (Map<String, Object> item : categoryItems) {
            item.put("count", ((Number) item.get("count")).intValue());
        }
        Map<String, Object> products = new LinkedHashMap<>();
        products.put("total", statsMapper.productTotal());
        products.put("low_stock", statsMapper.lowStockCount());
        products.put("by_category", categoryItems);
        result.put("products", products);

        return result;
    }
}