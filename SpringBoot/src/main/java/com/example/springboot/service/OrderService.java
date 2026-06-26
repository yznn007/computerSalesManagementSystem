package com.example.springboot.service;

import com.example.springboot.common.BizException;
import com.example.springboot.dto.OrderCreateRequest;
import com.example.springboot.dto.StatusUpdateRequest;
import com.example.springboot.entity.OrderDetail;
import com.example.springboot.entity.SalesOrder;
import com.example.springboot.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单业务层。
 * 下单与状态流转均委托数据库存储过程完成（事务 + 悲观锁防超卖、状态机校验、库存回补），
 * 本类只负责参数装配、解析存储过程的 OUT 状态码并翻译为业务异常。
 */
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final ObjectMapper objectMapper;

    public OrderService(OrderMapper orderMapper, ObjectMapper objectMapper) {
        this.orderMapper = orderMapper;
        this.objectMapper = objectMapper;
    }

    /** 下单，返回 {status, order_no}（status=0 成功） */
    public Map<String, Object> create(OrderCreateRequest req) {
        String itemsJson;
        try {
            // 商品清单序列化为 JSON 数组字符串传给存储过程，形如 [{"product_id":1,"quantity":2}]
            itemsJson = objectMapper.writeValueAsString(req.getItems());
        } catch (Exception e) {
            throw new BizException("参数序列化失败");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("customerId", req.getCustomerId());
        params.put("items", itemsJson);
        params.put("status", null);   // OUT：存储过程返回的结果码
        params.put("orderNo", null);  // OUT：成功时返回的订单号

        orderMapper.callCreateOrder(params);

        // OUT 为空按系统异常(4)处理；非 0 状态码翻译成中文错误消息抛出
        int status = params.get("status") == null ? 4 : ((Number) params.get("status")).intValue();
        String orderNo = String.valueOf(params.get("orderNo"));

        if (status != 0) {
            throw new BizException(mapCreateStatus(status));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", 0);
        result.put("order_no", orderNo);
        return result;
    }

    /** 轻量查询：仅取订单主信息（用于权限校验），不存在抛 404 */
    public SalesOrder getRawOrder(Integer id) {
        SalesOrder order = orderMapper.findById(id);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        return order;
    }

    /** 销售员：查看所有订单（可按状态筛选） */
    public List<SalesOrder> list(String status) {
        return orderMapper.findAll(status, null);
    }

    /** 客户：查看自己的订单（可按状态筛选） */
    public List<SalesOrder> listByCustomer(Integer customerId, String status) {
        return orderMapper.findAll(status, customerId);
    }

    /** 订单详情：订单主信息 + 明细列表 */
    public Map<String, Object> detail(Integer id) {
        SalesOrder order = orderMapper.findById(id);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        List<OrderDetail> items = orderMapper.findDetailsByOrderId(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);
        result.put("items", items);
        return result;
    }

    /** 状态流转（pay/ship/cancel/return），由存储过程内的状态机校验合法性并按需回补库存 */
    public void updateStatus(Integer id, StatusUpdateRequest req) {
        String action = req.getAction();
        if (!List.of("pay", "ship", "cancel", "return").contains(action)) {
            throw new BizException("操作类型必须为 pay/ship/cancel/return");
        }
        // 付款必须带支付方式，取消必须带原因（前置校验，减少无效存储过程调用）
        if ("pay".equals(action) && (req.getPaymentMethod() == null || req.getPaymentMethod().isBlank())) {
            throw new BizException("付款需提供支付方式");
        }
        if ("cancel".equals(action) && (req.getCancelReason() == null || req.getCancelReason().isBlank())) {
            throw new BizException("取消订单需提供原因");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("orderId", id);
        params.put("action", action);
        params.put("paymentMethod", req.getPaymentMethod());
        params.put("cancelReason", req.getCancelReason());
        params.put("status", null);   // OUT：0成功/1非法流转/2订单不存在/3系统异常
        params.put("message", null);  // OUT：失败时的中文提示

        orderMapper.callUpdateStatus(params);

        int status = params.get("status") == null ? 3 : ((Number) params.get("status")).intValue();
        if (status != 0) {
            String message = String.valueOf(params.get("message"));
            int httpCode = status == 2 ? 404 : 400; // 订单不存在映射 404，其余非法流转映射 400
            throw new BizException(httpCode, message);
        }
    }

    /** 将 sp_create_order 的状态码翻译为中文错误消息（0 成功不在此处理） */
    private String mapCreateStatus(int status) {
        return switch (status) {
            case 1 -> "商品数量不合法";
            case 2 -> "商品不存在";
            case 3 -> "库存不足";
            case 4 -> "系统异常，下单失败";
            default -> "下单失败";
        };
    }
}
