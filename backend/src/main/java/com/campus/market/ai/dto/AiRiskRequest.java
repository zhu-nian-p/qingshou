package com.campus.market.ai.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** AI 发布风险审核请求。价格只作为弱信号，可以为空。 */
@Data
public class AiRiskRequest {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "商品标题不能超过 100 个字符")
    private String title;

    @Size(max = 2000, message = "商品描述不能超过 2000 个字符")
    private String description;

    @Size(max = 32, message = "分类名称不能超过 32 个字符")
    private String category;

    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    @Digits(integer = 8, fraction = 2, message = "价格最多 8 位整数、2 位小数")
    private BigDecimal price;
}
