package com.campus.market.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.security.JwtUtil;
import com.campus.market.security.UserContext;
import com.campus.market.user.dto.LoginRequest;
import com.campus.market.user.dto.LoginResponse;
import com.campus.market.user.dto.RegisterRequest;
import com.campus.market.user.dto.RegisterResponse;
import com.campus.market.user.dto.ResetPasswordRequest;
import com.campus.market.user.dto.UserProfileVO;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailCodeService emailCodeService;

    public RegisterResponse register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String studentId = request.getStudentId().trim();

        boolean usernameTaken = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (usernameTaken) {
            throw BusinessException.badRequest("用户名已被注册");
        }

        boolean studentIdTaken = userMapper.exists(new LambdaQueryWrapper<User>()
                .eq(User::getStudentId, studentId));
        if (studentIdTaken) {
            throw BusinessException.badRequest("该学号已被注册");
        }

        // 学校邮箱验证码校验（匹配成功会立刻从 Redis 删除，防止重复使用）
        emailCodeService.verifyAndConsume(request.getSchoolEmail(), request.getEmailCode());

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName().trim());
        user.setStudentId(studentId);
        user.setSchoolEmail(request.getSchoolEmail().trim().toLowerCase(Locale.ROOT));
        user.setStatus(0);
        user.setRole(1);
        user.setCreditScore(100);
        userMapper.insert(user);

        return new RegisterResponse(user.getId(), user.getUsername());
    }

    public LoginResponse login(LoginRequest request) {        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername().trim()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw BusinessException.forbidden("账号已被封禁");
        }

        String token = jwtUtil.createToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole());
    }

    /** 忘记密码：校验邮箱验证码后重置密码 */
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getSchoolEmail, email));
        if (users.isEmpty()) {
            throw BusinessException.badRequest("该邮箱尚未注册");
        }
        // 验证码一次性使用，校验通过即删除
        emailCodeService.verifyAndConsumeReset(email, request.getEmailCode());
        userMapper.updatePassword(users.get(0).getId(), passwordEncoder.encode(request.getNewPassword()));
    }

    public UserProfileVO currentProfile() {        Long userId = UserContext.get();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.unauthorized("未登录或登录已过期");
        }

        UserProfileVO profile = new UserProfileVO();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setRealName(user.getRealName());
        profile.setStudentId(user.getStudentId());
        profile.setSchoolEmail(user.getSchoolEmail());
        profile.setStatus(user.getStatus());
        profile.setRole(user.getRole());
        profile.setCreditScore(user.getCreditScore());
        profile.setCreateTime(user.getCreateTime());
        return profile;
    }
}
