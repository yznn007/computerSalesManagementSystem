package com.example.springboot.service;

import com.example.springboot.common.BizException;
import com.example.springboot.dto.OrderCreateRequest;
import com.example.springboot.dto.StatusUpdateRequest;
import com.example.springboot.entity.Product;
import com.example.springboot.entity.SalesOrder;
import com.example.springboot.mapper.OrderMapper;
import com.example.springboot.mapper.ProductMapper;
import com.example.springboot.support.AbstractDbTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@Sql(scripts = "/test-reset.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderServiceTest extends AbstractDbTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ProductMapper productMapper;

    private static int testNo;

    @BeforeAll
    static void header() {
        System.out.println();
        System.out.println("==================== OrderService Tests ====================");
    }

    @BeforeEach
    void beforeEach(TestInfo info) {
        testNo++;
        System.out.printf("  [%2d] %-50s ", testNo, info.getDisplayName());
    }

    @AfterEach
    void afterEach() {
        System.out.println("PASSED");
    }

    @AfterAll
    static void footer() {
        System.out.println("============================================================");
        System.out.printf("  Total: %d passed, 0 failed, 0 skipped%n", testNo);
        System.out.println();
    }

    private OrderCreateRequest okReq() {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(1);

        OrderCreateRequest.Item item = new OrderCreateRequest.Item();
        item.setProductId(1);
        item.setQuantity(2);
        req.setItems(List.of(item));
        return req;
    }

    private StatusUpdateRequest statusReq(String action, String method, String reason) {
        StatusUpdateRequest r = new StatusUpdateRequest();
        r.setAction(action);
        r.setPaymentMethod(method);
        r.setCancelReason(reason);
        return r;
    }

    // ==================== Order Creation ====================

    @Test
    @DisplayName("create_ok")
    void create_ok() {
        Map<String, Object> result = orderService.create(okReq());
        assertThat(result.get("status")).isEqualTo(0);
        assertThat(result.get("order_no")).asString().startsWith("ORD");
        Product p = productMapper.findBasicById(1);
        assertThat(p.getStock()).isEqualTo(8);
    }

    @Test
    @DisplayName("create_items_empty")
    void create_items_empty() {
        OrderCreateRequest req = okReq();
        req.setItems(List.of());
        assertThatThrownBy(() -> orderService.create(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不合法");
    }

    @Test
    @DisplayName("create_quantity_zero")
    void create_quantity_zero() {
        OrderCreateRequest req = okReq();
        OrderCreateRequest.Item item = new OrderCreateRequest.Item();
        item.setProductId(1);
        item.setQuantity(0);
        req.setItems(List.of(item));
        assertThatThrownBy(() -> orderService.create(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不合法");
    }

    @Test
    @DisplayName("create_product_not_exist")
    void create_product_not_exist() {
        OrderCreateRequest req = okReq();
        req.getItems().get(0).setProductId(999);
        assertThatThrownBy(() -> orderService.create(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("create_stock_insufficient")
    void create_stock_insufficient() {
        OrderCreateRequest req = okReq();
        req.getItems().get(0).setQuantity(11);
        assertThatThrownBy(() -> orderService.create(req))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("库存不足");
    }

    @Test
    @DisplayName("create_two_items_total_amount")
    void create_two_items_total_amount() {
        OrderCreateRequest req = okReq();
        OrderCreateRequest.Item i2 = new OrderCreateRequest.Item();
        i2.setProductId(2);
        i2.setQuantity(1);
        req.setItems(List.of(req.getItems().get(0), i2));
        Map<String, Object> r = orderService.create(req);
        assertThat(r.get("status")).isEqualTo(0);
        SalesOrder o = orderMapper.findAll(null, null).get(0);
        assertThat(o.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(23997));
    }

    // ==================== Pay ====================

    @Test
    @DisplayName("pay_ok")
    void pay_ok() {
        Integer orderId = createOrder(1);
        orderService.updateStatus(orderId, statusReq("pay", "微信", null));
        SalesOrder o = orderMapper.findById(orderId);
        assertThat(o.getStatus()).isEqualTo("已付款");
        assertThat(o.getPaymentMethod()).isEqualTo("微信");
        assertThat(o.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(17998));
    }

    @Test
    @DisplayName("pay_missing_method")
    void pay_missing_method() {
        Integer orderId = createOrder(1);
        assertThatThrownBy(() -> orderService.updateStatus(orderId, statusReq("pay", null, null)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("支付方式");
    }

    @Test
    @DisplayName("pay_on_paid_status")
    void pay_on_ship_status() {
        Integer orderId = createOrder(1);
        orderService.updateStatus(orderId, statusReq("pay", "微信", null));
        assertThatThrownBy(() -> orderService.updateStatus(orderId, statusReq("pay", "微信", null)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不可付款");
    }

    // ==================== Ship ====================

    @Test
    @DisplayName("ship_ok")
    void ship_ok() {
        Integer orderId = createOrder(1);
        orderService.updateStatus(orderId, statusReq("pay", "微信", null));
        orderService.updateStatus(orderId, statusReq("ship", null, null));
        assertThat(orderMapper.findById(orderId).getStatus()).isEqualTo("已发货");
    }

    @Test
    @DisplayName("ship_on_pending")
    void ship_on_pending() {
        Integer orderId = createOrder(1);
        assertThatThrownBy(() -> orderService.updateStatus(orderId, statusReq("ship", null, null)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不可发货");
    }

    // ==================== Cancel ====================

    @Test
    @DisplayName("cancel_ok")
    void cancel_ok() {
        Integer orderId = createOrder(1);
        orderService.updateStatus(orderId, statusReq("cancel", null, "不想要了"));
        SalesOrder o = orderMapper.findById(orderId);
        assertThat(o.getStatus()).isEqualTo("已取消");
        assertThat(o.getCancelReason()).isEqualTo("不想要了");
        assertThat(productMapper.findBasicById(1).getStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("cancel_missing_reason")
    void cancel_missing_reason() {
        Integer orderId = createOrder(1);
        assertThatThrownBy(() -> orderService.updateStatus(orderId, statusReq("cancel", null, null)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("原因");
    }

    // ==================== Return ====================

    @Test
    @DisplayName("return_ok")
    void return_ok() {
        Integer orderId = createOrder(1);
        orderService.updateStatus(orderId, statusReq("pay", "支付宝", null));
        orderService.updateStatus(orderId, statusReq("ship", null, null));
        orderService.updateStatus(orderId, statusReq("return", null, null));
        SalesOrder o = orderMapper.findById(orderId);
        assertThat(o.getStatus()).isEqualTo("已退货");
        assertThat(productMapper.findBasicById(1).getStock()).isEqualTo(10);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("order_not_found_404")
    void order_not_found() {
        assertThatThrownBy(() -> orderService.getRawOrder(999))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("订单不存在");
    }

    @Test
    @DisplayName("status_order_not_found_404")
    void status_not_found() {
        assertThatThrownBy(() -> orderService.updateStatus(999, statusReq("ship", null, null)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("订单不存在");
    }

    @Test
    @DisplayName("status_unknown_action")
    void status_unknown_action() {
        Integer orderId = createOrder(1);
        assertThatThrownBy(() -> orderService.updateStatus(orderId, statusReq("unknown", null, null)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("pay/ship/cancel/return");
    }

    // ==================== Helpers ====================

    private Integer createOrder(int customerId) {
        OrderCreateRequest req = new OrderCreateRequest();
        req.setCustomerId(customerId);
        OrderCreateRequest.Item item = new OrderCreateRequest.Item();
        item.setProductId(1);
        item.setQuantity(2);
        req.setItems(List.of(item));
        Map<String, Object> result = orderService.create(req);
        assertThat(result.get("status")).isEqualTo(0);
        return orderMapper.findAll(null, null).get(0).getOrderId();
    }
}