package com.campus.market.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** AI 生成商品描述的请求 */
@Data
public class AiDescRequest {

    @NotBlank(message = "商品关键词不能为空")
    @Size(max = 50, message = "关键词不能超过 50 个字符")
    private String keyword;
}
