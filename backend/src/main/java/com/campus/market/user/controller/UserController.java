package com.campus.market.user.controller;

import com.campus.market.common.Result;
import com.campus.market.user.dto.LoginRequest;
import com.campus.market.user.dto.LoginResponse;
import com.campus.market.user.dto.RegisterRequest;
import com.campus.market.user.dto.RegisterResponse;
import com.campus.market.user.dto.UserProfileVO;
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

    @PostMapping("/register")
    public Result<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("青售提示：注册成功", userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("青售提示：登录成功", userService.login(request));
    }

    /** 需要在请求头携带 Authorization: Bearer {token}，用来确认登录态。 */
    @GetMapping("/me")
    public Result<UserProfileVO> me() {
        return Result.success(userService.currentProfile());
    }
}
