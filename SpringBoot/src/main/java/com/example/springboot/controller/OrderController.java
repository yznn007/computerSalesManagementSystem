package com.example.springboot.controller;

import com.example.springboot.common.BizException;
import com.example.springboot.dto.OrderCreateRequest;
import com.example.springboot.dto.StatusUpdateRequest;
import com.example.springboot.entity.SalesOrder;
import com.example.springboot.security.AuthContext;
import com.example.springboot.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单接口（/api/orders）。
 * 权限按角色区分：客户只能操作/查看自己的订单且不能代付以外越权；
 * 销售员可查全部、可代客下单与发货/退货/取消，但不能代客户付款。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 下单：客户用 token 的 id（防伪造），销售员代客下单需在 body 指定 customer_id */
    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody OrderCreateRequest req) {
        AuthContext.CurrentUser user = AuthContext.require();
        // 客户下单：用 token 中的 customer_id（忽略 body 中可能伪造的 customer_id）
        // 销售员代客下单：使用 body 中的 customer_id
        if (AuthContext.ROLE_CUSTOMER.equals(user.role())) {
            req.setCustomerId(user.id().intValue());
        } else {
            // 销售员或其他：必须提供 customer_id
            if (req.getCustomerId() == null) {
                throw new BizException("请选择客户");
            }
        }
        return orderService.create(req);
    }

    /** 订单列表：销售员看全部，客户仅看自己的（均可按状态筛选） */
    @GetMapping
    public List<SalesOrder> list(@RequestParam(required = false) String status) {
        AuthContext.CurrentUser user = AuthContext.require();
        if (AuthContext.ROLE_STAFF.equals(user.role())) {
            return orderService.list(status);
        }
        // 客户只能看自己的订单（可按状态筛选）
        return orderService.listByCustomer(user.id().intValue(), status);
    }

    /** 订单详情：客户访问他人订单时抛 403 */
    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Integer id) {
        AuthContext.CurrentUser user = AuthContext.require();
        Map<String, Object> result = orderService.detail(id);
        SalesOrder order = (SalesOrder) result.get("order");
        // 客户只能查看自己的订单
        if (AuthContext.ROLE_CUSTOMER.equals(user.role())
                && !order.getCustomerId().equals(user.id().intValue())) {
            throw new BizException(403, "无权查看该订单");
        }
        return result;
    }

    /** 状态流转：销售员可 ship/return/cancel（不可 pay）；客户仅可对自己订单 pay/cancel */
    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Integer id, @Valid @RequestBody StatusUpdateRequest req) {
        AuthContext.CurrentUser user = AuthContext.require();
        String action = req.getAction();

        if (AuthContext.ROLE_STAFF.equals(user.role())) {
            // 销售员：仅可发货/退货/取消，不可代客户付款
            if ("pay".equals(action)) {
                throw new BizException(403, "付款须由客户本人完成");
            }
        } else if (AuthContext.ROLE_CUSTOMER.equals(user.role())) {
            // 客户：仅可对自己的订单执行付款/取消
            if (!"pay".equals(action) && !"cancel".equals(action)) {
                throw new BizException(403, "客户仅可付款或取消订单");
            }
            SalesOrder order = orderService.getRawOrder(id);
            if (!order.getCustomerId().equals(user.id().intValue())) {
                throw new BizException(403, "无权操作该订单");
            }
        } else {
            throw new BizException(403, "无权执行该操作");
        }
        orderService.updateStatus(id, req);
    }
}
