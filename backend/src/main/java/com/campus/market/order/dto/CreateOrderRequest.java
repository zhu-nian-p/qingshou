package com.campus.market.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 买家下单请求体 */
@Data
public class CreateOrderRequest {

    @NotNull(message = "商品ID不能为空")
    private Long itemId;

    @NotBlank(message = "交易地点不能为空")
    @Size(max = 200, message = "交易地点不能超过 200 个字符")
    private String tradeLocation;
}
