package com.example.springboot.controller;

import com.example.springboot.dto.OrderCreateRequest;
import com.example.springboot.dto.StatusUpdateRequest;
import com.example.springboot.entity.SalesOrder;
import com.example.springboot.security.AuthContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final com.example.springboot.service.OrderService orderService;

    public OrderController(com.example.springboot.service.OrderService orderService) {
        this.orderService = orderService;
    }

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
                throw new com.example.springboot.common.BizException("请选择客户");
            }
        }
        return orderService.create(req);
    }

    @GetMapping
    public List<SalesOrder> list(@RequestParam(required = false) String status) {
        AuthContext.CurrentUser user = AuthContext.require();
        if (AuthContext.ROLE_STAFF.equals(user.role())) {
            return orderService.list(status);
        }
        // 客户只能看自己的订单
        return orderService.listByCustomer(user.id().intValue());
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable Integer id) {
        AuthContext.CurrentUser user = AuthContext.require();
        Map<String, Object> result = orderService.detail(id);
        com.example.springboot.entity.SalesOrder order =
                (com.example.springboot.entity.SalesOrder) result.get("order");
        // 客户只能查看自己的订单
        if (AuthContext.ROLE_CUSTOMER.equals(user.role())
                && !order.getCustomerId().equals(user.id().intValue())) {
            throw new com.example.springboot.common.BizException(403, "无权查看该订单");
        }
        return result;
    }

    @PutMapping("/{id}/status")
    public void updateStatus(@PathVariable Integer id, @Valid @RequestBody StatusUpdateRequest req) {
        AuthContext.requireStaff();
        orderService.updateStatus(id, req);
    }
}
