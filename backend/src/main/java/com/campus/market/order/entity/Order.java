package com.campus.market.order.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对应表 order。
 * <p>
 * order 是 MySQL 保留字：application.yml 里的 table-format 会给 MyBatis-Plus
 * 自动生成的 TableName 加上反引号；自己写 @Update/@Select 时已显式写成 `order`。
 */
@Data
@TableName("order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;

    private Long buyerId;

    private Long sellerId;

    private String tradeLocation;

    /** 3=待卖家确认，0=待面交，1=已完成，2=已取消 */
    private Integer status;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
