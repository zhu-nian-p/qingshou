package com.campus.market.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 发布成功后的回执，只返回必要字段，方便前端跳转到详情页 */
@Data
@AllArgsConstructor
public class PublishItemResponse {

    private Long id;

    private String title;

    private BigDecimal price;

    private Integer status;

    private Integer isTop;

    private LocalDateTime createTime;

    /** LOW / MEDIUM；HIGH 不会返回成功响应 */
    private String riskLevel;

    /** MEDIUM 时为风险原因；LOW 时为空列表 */
    private List<String> riskWarnings;
}
