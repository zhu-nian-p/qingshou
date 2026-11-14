package com.campus.market.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单通知 WebSocket：按 userId 维护连接，支持同一用户多端在线。
 * <p>
 * 连接地址：ws://host/ws?token={JWT}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderWebSocketHandler extends TextWebSocketHandler {

    public static final String ATTR_USER_ID = "userId";

    private final ObjectMapper objectMapper;

    /** userId -> 该用户的全部在线连接 */
    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get(ATTR_USER_ID);
        if (userId == null) {
            try {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("unauthorized"));
            } catch (Exception ignored) {
            }
            return;
        }
        sessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WebSocket 连接建立: userId={}, sessionId={}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 简单心跳：客户端发 ping，服务端回 pong
        if ("ping".equalsIgnoreCase(message.getPayload())) {
            synchronized (session) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage("pong"));
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get(ATTR_USER_ID);
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> set = sessions.get(userId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) {
                sessions.remove(userId);
            }
        }
        log.info("WebSocket 连接关闭: userId={}, sessionId={}, status={}", userId, session.getId(), status);
    }

    /** 给指定用户的所有在线连接推送 JSON 消息 */
    public void sendToUser(Long userId, Object payload) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set == null || set.isEmpty()) {
            log.debug("用户 {} 当前没有在线 WebSocket 连接，跳过推送", userId);
            return;
        }
        try {
            String text = objectMapper.writeValueAsString(payload);
            for (WebSocketSession session : set) {
                try {
                    synchronized (session) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(text));
                        }
                    }
                } catch (Exception e) {
                    log.warn("推送 WebSocket 消息失败: userId={}, sessionId={}, {}", userId, session.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.warn("序列化 WebSocket 消息失败: {}", e.getMessage());
        }
    }

    /** 当前在线用户数（调试/监控用） */
    public int onlineUsers() {
        return sessions.size();
    }
}
