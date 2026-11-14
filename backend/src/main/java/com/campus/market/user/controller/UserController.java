package com.campus.market.user.controller;

import com.campus.market.common.Result;
import com.campus.market.user.dto.LoginRequest;
import com.campus.market.user.dto.LoginResponse;
import com.campus.market.user.dto.RegisterRequest;
import com.campus.market.user.dto.RegisterResponse;
import com.campus.market.user.dto.ResetPasswordRequest;
import com.campus.market.user.dto.SendCodeRequest;
import com.campus.market.user.dto.UserProfileVO;
import com.campus.market.user.service.EmailCodeService;
import com.campus.market.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailCodeService emailCodeService;

    /**
     * 发送学校邮箱验证码（无需登录）。
     * 同一邮箱 1 分钟内只能发送一次，验证码 5 分钟内有效。
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        emailCodeService.sendCode(request.getEmail());
        return Result.success("青售提示：验证码已发送，请查收学校邮箱（5 分钟内有效）");
    }

    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("青售提示：注册成功", userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("青售提示：登录成功", userService.login(request));
    }

    /** 忘记密码：发送邮箱验证码（无需登录） */
    @PostMapping("/send-reset-code")
    public Result<Void> sendResetCode(@Valid @RequestBody SendCodeRequest request) {
        emailCodeService.sendResetCode(request.getEmail());
        return Result.success("青售提示：验证码已发送，请查收学校邮箱（5 分钟内有效）");
    }

    /** 忘记密码：用邮箱验证码重置密码（无需登录） */
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return Result.success("青售提示：密码重置成功，请用新密码登录");
    }

    /** 需要在请求头携带 Authorization: Bearer {token}，用来确认登录态。 */
    @GetMapping("/me")
    public Result<UserProfileVO> me() {
        return Result.success(userService.currentProfile());
    }
}
