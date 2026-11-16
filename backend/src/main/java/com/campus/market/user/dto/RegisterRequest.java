package com.campus.market.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 10, message = "昵称长度需为 2-10 个字符")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名不能超过 50 个字符")
    private String realName;

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^\\d{8,9}$", message = "学号必须是 8 到 9 位数字")
    private String studentId;

    @NotBlank(message = "校园邮箱不能为空")
    @Email(message = "校园邮箱格式不正确")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@stu\\.nchu\\.edu\\.cn$",
            message = "仅支持 @stu.nchu.edu.cn 学校邮箱")
    @Size(max = 100, message = "校园邮箱不能超过 100 个字符")
    private String schoolEmail;

    @NotBlank(message = "邮箱验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "邮箱验证码必须是 6 位数字")
    private String emailCode;
}
