package com.campus.market.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员直接置顶请求（人工审核线下付款后手动点确认）。
 * 等价于 POST /api/admin/items/{id}/top?days=N，只是改用请求体传参。
 */
@Data
public class TopItemRequest {

    @NotNull(message = "商品ID不能为空")
    private Long itemId;

    @NotNull(message = "置顶天数不能为空")
    @Min(value = 1, message = "置顶天数最少 1 天")
    @Max(value = 365, message = "置顶天数最多 365 天")
    private Integer days;
}
