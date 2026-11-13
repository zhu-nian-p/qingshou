package com.campus.market.topup.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 置顶申请：0=待审核，1=已通过（已置顶），2=已拒绝。
 * 金额 amount 由后端按阶梯价计算，前端传的金额一律不认。
 */
@Data
@TableName("top_up_apply")
public class TopUpApply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long itemId;

    private Long userId;

    /** 1 / 7 / 30 天 */
    private Integer days;

    /** 应付金额（元） */
    private BigDecimal amount;

    private Integer status;

    /** 拒绝原因（仅 status=2 时有值） */
    private String rejectReason;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
