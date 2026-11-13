package com.campus.market.topup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员拒绝置顶申请，必须填写原因 */
@Data
public class RejectTopUpRequest {

    @NotBlank(message = "拒绝原因不能为空")
    @Size(max = 255, message = "拒绝原因不能超过 255 个字符")
    private String reason;
}
