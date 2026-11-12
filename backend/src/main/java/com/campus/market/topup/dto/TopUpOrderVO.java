package com.campus.market.topup.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 置顶订单返回体 */
@Data
public class TopUpOrderVO {

    private Long id;

    private Long itemId;

    private String itemTitle;

    private Long userId;

    private Integer days;

    private BigDecimal price;

    /** 0=待审核，1=已支付/已生效，2=已驳回 */
    private Integer status;

    private String statusText;

    private LocalDateTime createTime;
}
