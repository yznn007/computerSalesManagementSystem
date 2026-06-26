package com.example.springboot.service;

import com.example.springboot.mapper.StatsMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售看板统计业务层（仅供销售员）。
 * 汇总客户/订单/销售额/商品四个维度的统计指标，组装成嵌套 Map 供前端图表直接消费。
 */
@Service
public class StatsService {

    private final StatsMapper statsMapper;

    public StatsService(StatsMapper statsMapper) {
        this.statsMapper = statsMapper;
    }

    /** 看板总览：返回 customers / orders / sales / products 四组指标（用 LinkedHashMap 保序） */
    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 客户统计
        Map<String, Object> customers = new LinkedHashMap<>();
        long customerTotal = statsMapper.customerTotal();
        customers.put("total", customerTotal);
        customers.put("active", statsMapper.activeCustomerCount());
        long orderTotal = statsMapper.orderTotal();
        // 客均订单数 = 订单总数 / 客户总数（无客户时回退 0.0，避免除零）
        customers.put("avg_orders", customerTotal > 0 ? String.format("%.1f", (double) orderTotal / customerTotal) : "0.0");
        result.put("customers", customers);

        // 订单统计
        List<Map<String, Object>> statusItems = statsMapper.orderCountByStatus();
        for (Map<String, Object> item : statusItems) {
            item.put("count", ((Number) item.get("count")).intValue()); // 统一 count 为 int，规避 JDBC 返回 Long/BigInteger
        }
        Map<String, Object> orders = new LinkedHashMap<>();
        orders.put("total", orderTotal);
        orders.put("by_status", statusItems);
        result.put("orders", orders);

        // 销售额统计
        Map<String, Object> sales = new LinkedHashMap<>();
        sales.put("paid", statsMapper.paidAmount());         // 成交订单金额
        sales.put("pending", statsMapper.pendingAmount());   // 待付款金额
        sales.put("received", statsMapper.receivedAmount()); // 实收金额
        result.put("sales", sales);

        // 商品统计
        List<Map<String, Object>> categoryItems = statsMapper.productCountByCategory();
        for (Map<String, Object> item : categoryItems) {
            item.put("count", ((Number) item.get("count")).intValue());
        }
        Map<String, Object> products = new LinkedHashMap<>();
        products.put("total", statsMapper.productTotal());
        products.put("low_stock", statsMapper.lowStockCount()); // 低库存告警数量
        products.put("by_category", categoryItems);
        result.put("products", products);

        return result;
    }
}