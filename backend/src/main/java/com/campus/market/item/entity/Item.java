package com.campus.market.item.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对应表 item。
 * 时间字段交给数据库默认值与 ON UPDATE，插入/更新时不写。
 */
@Data
@TableName("item")
public class Item {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 卖家用户 ID，来自 JWT，不允许前端传 */
    private Long sellerId;

    private String title;

    private String description;

    /** 售价（元），DECIMAL(10,2) */
    private BigDecimal price;

    private String imageUrl;

    /** 分类：数码 / 书籍 / 生活用品 等 */
    private String category;

    /** 卖家联系方式（微信/QQ） */
    private String contactInfo;

    /** 0=在售 1=已预订 2=已售出 3=违规下架 */
    private Integer status;

    /** 0=普通 1=置顶 */
    private Integer isTop;

    private LocalDateTime topExpireTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
