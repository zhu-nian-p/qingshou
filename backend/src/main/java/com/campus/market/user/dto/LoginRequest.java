package com.campus.market.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "学号不能为空")
    @Pattern(regexp = "^\\d{8,9}$", message = "学号必须是 8 到 9 位数字")
    private String studentId;

    @NotBlank(message = "密码不能为空")
    private String password;
}
