package com.campus.market.item.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品详情。
 * <p>
 * contactInfo 只在「已登录」时下发：
 * 未登录访问时该字段为 null，前端可以据此提示「登录后查看联系方式」。
 */
@Data
public class ItemDetailVO {

    private Long id;

    private Long sellerId;

    private String title;

    private String description;

    private BigDecimal price;

    private String imageUrl;

    private String category;

    /** 所属校区：上海路 / 前湖 */
    private String campus;

    /** 0=在售 1=已预订 2=已售出 3=违规下架 */
    private Integer status;

    private Integer isTop;

    private LocalDateTime topExpireTime;

    private LocalDateTime createTime;

    /** 卖家联系方式；未登录时为 null */
    private String contactInfo;
}
