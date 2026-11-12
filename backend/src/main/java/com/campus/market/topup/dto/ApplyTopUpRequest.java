package com.campus.market.topup.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 申请置顶 */
@Data
public class ApplyTopUpRequest {

    @NotNull(message = "商品ID不能为空")
    private Long itemId;

    @NotNull(message = "置顶天数不能为空")
    @Min(value = 1, message = "置顶天数最少 1 天")
    @Max(value = 30, message = "置顶天数最多 30 天")
    private Integer days;
}
