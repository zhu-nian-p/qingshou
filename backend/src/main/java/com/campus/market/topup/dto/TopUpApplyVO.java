package com.campus.market.topup.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 置顶申请返回体 */
@Data
public class TopUpApplyVO {

    private Long id;

    private Long itemId;

    /** 商品名称（管理员审核列表与我的申请都需要） */
    private String itemTitle;

    private Long userId;

    /** 申请人展示名（真实姓名 + 用户名） */
    private String userName;

    private Integer days;

    /** 后端计算的应付金额 */
    private BigDecimal amount;

    /** 0=待审核，1=已通过，2=已拒绝 */
    private Integer status;

    private String statusText;

    /** 拒绝原因（仅 status=2 时有值） */
    private String rejectReason;

    private LocalDateTime createTime;
}
