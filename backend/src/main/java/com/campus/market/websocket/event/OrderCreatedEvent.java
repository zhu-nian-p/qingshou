package com.campus.market.websocket.event;

/**
 * 买家下单成功事件：在事务提交后推送给卖家。
 */
public record OrderCreatedEvent(Long sellerId, Long orderId, Long itemId, String itemTitle) {
}
