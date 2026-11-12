package com.campus.market.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单返回体，带上商品快照字段方便前端直接渲染列表 */
@Data
public class OrderVO {

    private Long id;

    private Long itemId;

    /** 商品标题快照（下单时商品的标题） */
    private String itemTitle;

    private BigDecimal itemPrice;

    private String itemImageUrl;

    private Long buyerId;

    private Long sellerId;

    private String tradeLocation;

    /** 3=待卖家确认，0=待面交，1=已完成，2=已取消 */
    private Integer status;

    /** 状态中文，前端可直接显示 */
    private String statusText;

    private LocalDateTime createTime;
}
