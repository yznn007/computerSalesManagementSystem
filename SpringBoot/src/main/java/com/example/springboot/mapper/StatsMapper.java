package com.example.springboot.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface StatsMapper {

    @Select("SELECT status, COUNT(*) AS count FROM Sales_Order GROUP BY status")
    List<Map<String, Object>> orderCountByStatus();

    @Select("SELECT COUNT(*) FROM Sales_Order")
    long orderTotal();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM Sales_Order WHERE status IN ('已付款', '已发货')")
    Object paidAmount();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM Sales_Order WHERE status = '待付款'")
    Object pendingAmount();

    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM Sales_Order WHERE status IN ('已付款', '已发货')")
    Object receivedAmount();

    @Select("SELECT category, COUNT(*) AS count FROM Product GROUP BY category")
    List<Map<String, Object>> productCountByCategory();

    @Select("SELECT COUNT(*) FROM Product")
    long productTotal();

    @Select("SELECT COUNT(*) FROM Product WHERE stock <= 5")
    long lowStockCount();

    @Select("SELECT COUNT(*) FROM Customer")
    long customerTotal();

    @Select("SELECT COUNT(DISTINCT customer_id) FROM Sales_Order WHERE status IN ('已付款', '已发货')")
    long activeCustomerCount();
}