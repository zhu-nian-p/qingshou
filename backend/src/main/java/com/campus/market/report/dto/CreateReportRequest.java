package com.campus.market.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 提交举报：itemId 与 userId 二选一 */
@Data
public class CreateReportRequest {

    private Long itemId;

    private Long userId;

    @NotBlank(message = "请填写举报原因")
    @Size(max = 500, message = "举报原因不能超过 500 个字符")
    private String reason;
}
