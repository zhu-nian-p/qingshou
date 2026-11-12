package com.campus.market.topup.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 置顶购买订单：0=待审核，1=已支付/已生效，2=已驳回 */
@Data
@TableName("top_up_order")
public class TopUpOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;

    private Long userId;

    private Integer days;

    private BigDecimal price;

    private Integer status;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
