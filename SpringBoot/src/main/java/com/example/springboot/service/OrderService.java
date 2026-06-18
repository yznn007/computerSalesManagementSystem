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
            itemsJson = objectMapper.writeValueAsString(req.getItems());
        } catch (Exception e) {
            throw new BizException("参数序列化失败");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("customerId", req.getCustomerId());
        params.put("items", itemsJson);
        params.put("status", null);
        params.put("orderNo", null);

        orderMapper.callCreateOrder(params);

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

    /** 状态流转 */
    public void updateStatus(Integer id, StatusUpdateRequest req) {
        String action = req.getAction();
        if (!List.of("pay", "ship", "cancel", "return").contains(action)) {
            throw new BizException("操作类型必须为 pay/ship/cancel/return");
        }
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
        params.put("status", null);
        params.put("message", null);

        orderMapper.callUpdateStatus(params);

        int status = params.get("status") == null ? 3 : ((Number) params.get("status")).intValue();
        if (status != 0) {
            String message = String.valueOf(params.get("message"));
            int httpCode = status == 2 ? 404 : 400;
            throw new BizException(httpCode, message);
        }
    }

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
