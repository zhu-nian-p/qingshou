package com.campus.market.report.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 举报返回体（管理后台列表 / 提交结果） */
@Data
public class ReportVO {

    private Long id;

    private Long itemId;

    private String itemTitle;

    private Long targetUserId;

    private String targetUserName;

    private Long reporterId;

    private String reporterName;

    private String reason;

    private Integer status;

    private String statusText;

    private Long handlerId;

    private LocalDateTime handleTime;

    private LocalDateTime createTime;
}
