package com.campus.market.item.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发布商品请求体。
 * seller_id 一律从 JWT 取，不允许前端传入。
 */
@Data
public class PublishItemRequest {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "商品标题不能超过 100 个字符")
    private String title;

    @Size(max = 2000, message = "商品描述不能超过 2000 个字符")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    @Digits(integer = 8, fraction = 2, message = "价格最多 8 位整数、2 位小数")
    private BigDecimal price;

    @Size(max = 500, message = "图片链接不能超过 500 个字符")
    private String imageUrl;

    @NotBlank(message = "商品分类不能为空")
    @Size(max = 32, message = "分类名称不能超过 32 个字符")
    private String category;

    /** 校区：上海路 / 前湖，不传默认「前湖」 */
    @Size(max = 32, message = "校区名称不能超过 32 个字符")
    private String campus;

    @Size(max = 100, message = "联系方式不能超过 100 个字符")
    private String contactInfo;
}
