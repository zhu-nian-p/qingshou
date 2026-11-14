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

    /** 卖家用户名（展示用） */
    private String sellerName;

    /** 卖家信用分，前端据此显示「🏅 高信誉」 */
    private Integer sellerCredit;

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

    /** 卖家联系方式掩码（如 wx: ali***01）；完整值需调用 /api/item/{id}/contact，且限频 */
    private String contactMasked;
}
