package com.campus.market.report.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 举报记录：0=待处理，1=已处理 */
@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被举报商品ID（与 targetUserId 二选一） */
    private Long itemId;

    /** 被举报用户ID（与 itemId 二选一） */
    private Long targetUserId;

    private Long reporterId;

    private String reason;

    private Integer status;

    private Long handlerId;

    private LocalDateTime handleTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}
