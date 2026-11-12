package com.campus.market.review.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 评价返回体 */
@Data
public class ReviewVO {

    private Long id;

    private Long orderId;

    private Long reviewerId;

    private Long targetUserId;

    private Integer rating;

    private String content;

    /** 本次评价对卖家信用分的影响，例如 5 星为 +2 */
    private Integer creditDelta;

    private LocalDateTime createTime;
}
