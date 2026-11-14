package com.campus.market.websocket;

import com.campus.market.security.JwtUtil;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * WebSocket 握手鉴权：从 ?token= 取 JWT，校验通过后把 userId 写进 session 属性。
 * 浏览器 WebSocket 无法设置 Authorization 头，因此用查询参数传递 token。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .getFirst("token");

        if (token == null || token.isBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Long userId;
        try {
            userId = jwtUtil.parseUserId(token);
        } catch (JwtException | IllegalArgumentException e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        if (userId == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }

        attributes.put(OrderWebSocketHandler.ATTR_USER_ID, userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
