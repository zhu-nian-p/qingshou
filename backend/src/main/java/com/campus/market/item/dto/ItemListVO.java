package com.campus.market.item.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 列表项。故意不包含 contactInfo：
 * 联系方式只在详情页且已登录时返回，避免被批量抓取。
 */
@Data
public class ItemListVO {

    private Long id;

    private Long sellerId;

    /** 卖家用户名，列表展示用（故意不用真实姓名，避免隐私泄露） */
    private String sellerName;

    /** 卖家信用分，前端据此显示「🏅 高信誉」标签 */
    private Integer sellerCredit;

    private String title;

    private BigDecimal price;

    private String imageUrl;

    private String category;

    /** 所属校区：上海路 / 前湖 */
    private String campus;

    /** 0=在售 1=已预订 2=已售出 3=违规下架 */
    private Integer status;

    /** 1 表示置顶，列表永远置顶在前 */
    private Integer isTop;

    private LocalDateTime createTime;
}
