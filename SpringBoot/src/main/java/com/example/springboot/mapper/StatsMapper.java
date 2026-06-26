package com.example.springboot.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 销售报表统计数据访问层（仅供销售员看板使用）。
 * 全为只读聚合查询，金额用 COALESCE 兜底 0 避免无数据时返回 NULL。
 * 注意：status 列存中文枚举值（待付款/已付款/已发货等）。
 */
public interface StatsMapper {

    /** 各订单状态的数量分布 */
    @Select("SELECT status, COUNT(*) AS count FROM Sales_Order GROUP BY status")
    List<Map<String, Object>> orderCountByStatus();

    /** 订单总数 */
    @Select("SELECT COUNT(*) FROM Sales_Order")
    long orderTotal();

    /** 已成交订单金额合计（已付款 + 已发货）：成交总额 */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM Sales_Order WHERE status IN ('已付款', '已发货')")
    Object paidAmount();

    /** 待付款订单金额合计：潜在待收款 */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM Sales_Order WHERE status = '待付款'")
    Object pendingAmount();

    /** 实收金额合计（按 paid_amount 实付字段统计已成交订单） */
    @Select("SELECT COALESCE(SUM(paid_amount), 0) FROM Sales_Order WHERE status IN ('已付款', '已发货')")
    Object receivedAmount();

    /** 各分类商品数量分布（笔记本/台式机/配件） */
    @Select("SELECT category, COUNT(*) AS count FROM Product GROUP BY category")
    List<Map<String, Object>> productCountByCategory();

    /** 商品总数 */
    @Select("SELECT COUNT(*) FROM Product")
    long productTotal();

    /** 低库存商品数（库存 <= 5 视为告警阈值） */
    @Select("SELECT COUNT(*) FROM Product WHERE stock <= 5")
    long lowStockCount();

    /** 客户总数 */
    @Select("SELECT COUNT(*) FROM Customer")
    long customerTotal();

    /** 活跃客户数（有过成交订单的去重客户数） */
    @Select("SELECT COUNT(DISTINCT customer_id) FROM Sales_Order WHERE status IN ('已付款', '已发货')")
    long activeCustomerCount();
}