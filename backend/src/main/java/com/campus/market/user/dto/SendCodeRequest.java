package com.campus.market.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 发送学校邮箱验证码 */
@Data
public class SendCodeRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@stu\\.nchu\\.edu\\.cn$",
            message = "仅支持 @stu.nchu.edu.cn 学校邮箱")
    private String email;
}
