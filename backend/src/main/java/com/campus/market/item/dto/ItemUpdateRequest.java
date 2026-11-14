package com.campus.market.item.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/** 编辑在售商品 */
@Data
public class ItemUpdateRequest {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "标题不能超过 100 个字符")
    private String title;

    @Size(max = 2000, message = "描述不能超过 2000 个字符")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    @NotBlank(message = "分类不能为空")
    @Size(max = 32, message = "分类名称不能超过 32 个字符")
    private String category;

    @Size(max = 32, message = "校区名称不能超过 32 个字符")
    private String campus;

    @Size(max = 100, message = "联系方式不能超过 100 个字符")
    private String contactInfo;

    @Size(max = 500, message = "图片地址不能超过 500 个字符")
    private String imageUrl;
}
