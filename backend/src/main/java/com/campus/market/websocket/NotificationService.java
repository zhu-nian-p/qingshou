package com.campus.market.websocket;

import com.campus.market.websocket.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 业务通知：事务提交后再推送，避免订单回滚却已通知卖家。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final OrderWebSocketHandler webSocketHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCreated(OrderCreatedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "NEW_ORDER");
        payload.put("title", "青售提示");
        payload.put("message", "有新订单啦，请及时确认！");
        payload.put("orderId", event.orderId());
        payload.put("itemId", event.itemId());
        payload.put("itemTitle", event.itemTitle());
        webSocketHandler.sendToUser(event.sellerId(), payload);
        log.info("已向卖家 {} 推送新订单通知: orderId={}, item={}",
                event.sellerId(), event.orderId(), event.itemTitle());
    }
}
