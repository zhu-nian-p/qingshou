package com.campus.market.security;

import com.campus.market.common.Result;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跨域预检请求（OPTIONS）不带 token，必须放行，否则浏览器发起的跨域请求会直接 401
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            // 商品列表/详情允许匿名访问：没带 token 直接放行，UserContext 保持为空
            if (isOptionalAuth(request)) {
                return true;
            }
            writeJson(response, 401, "未登录或登录已过期");
            return false;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        Long userId;
        try {
            userId = jwtUtil.parseUserId(token);
        } catch (JwtException | IllegalArgumentException ex) {
            writeJson(response, 401, "未登录或登录已过期");
            return false;
        }
        if (userId == null) {
            writeJson(response, 401, "未登录或登录已过期");
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            writeJson(response, 401, "未登录或登录已过期");
            return false;
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            writeJson(response, 403, "账号已被封禁");
            return false;
        }

        UserContext.set(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 允许匿名访问的接口：商品列表与商品详情（GET /api/item/**）。
     * 带了 token 仍会解析并写入 UserContext，这样详情页能判断是否展示联系方式。
     */
    private boolean isOptionalAuth(HttpServletRequest request) {
        return "GET".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith("/api/item");
    }

    private void writeJson(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.fail(code, message));
    }
}
