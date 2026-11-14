package com.campus.market.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 管理员处理举报：delist=下架商品，dismiss=忽略 */
@Data
public class HandleReportRequest {

    @NotBlank(message = "处理动作不能为空")
    @Pattern(regexp = "delist|dismiss", message = "处理动作只能是 delist 或 dismiss")
    private String action;
}
