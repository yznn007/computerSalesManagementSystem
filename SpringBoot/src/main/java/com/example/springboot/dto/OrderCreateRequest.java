package com.example.springboot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    // 客户自助下单时不传，由控制器从 token 注入；销售员代客下单时必填，由控制器校验
    private Integer customerId;

    @NotEmpty(message = "商品清单不能为空")
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull(message = "商品不能为空")
        private Integer productId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为1")
        private Integer quantity;
    }
}
