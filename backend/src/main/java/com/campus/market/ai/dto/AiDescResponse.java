package com.campus.market.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** AI 生成的商品描述 */
@Data
@AllArgsConstructor
public class AiDescResponse {

    /** 生成好的中文文案（约 50 字） */
    private String description;

    /** 实际调用的模型；模拟数据时为 mock */
    private String model;

    /** true 表示没有配置 API Key，返回的是本地模拟文案 */
    private boolean mock;
}
