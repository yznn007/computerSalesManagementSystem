package com.example.springboot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建订单请求 DTO。
 * 客户自助下单时不传 customerId（由 token 注入），销售员代客下单时必填。
 * items 为商品清单，每项须指定 productId 与 quantity（≥1）。
 */
@Data
public class OrderCreateRequest {
    // 客户自助下单时不传，由控制器从 token 注入；销售员代客下单时必填，由控制器校验
    private Integer customerId; // 客户 ID（客户自助不传，销售员代客必填）

    @NotEmpty(message = "商品清单不能为空")
    @Valid
    private List<Item> items; // 商品清单

    @Data
    public static class Item {
        @NotNull(message = "商品不能为空")
        private Integer productId; // 商品 ID

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity; // 购买数量，最小为 1
    }
}
