package com.campus.market.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 用「学号 + 邮箱验证码」重置密码 */
@Data
public class ResetPasswordRequest {

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^\\d{8,9}$", message = "学号必须是 8 到 9 位数字")
    private String studentId;

    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "验证码不能为空")
    private String emailCode;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度需为 6-32 位")
    private String newPassword;
}
