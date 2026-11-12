package com.campus.market.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.order.entity.Order;
import com.campus.market.order.mapper.OrderMapper;
import com.campus.market.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动取消定时任务。
 * <p>
 * 规则：每 10 分钟扫描一次，状态为 3（待卖家确认）且创建时间超过 24 小时的订单，
 * 自动置为 2（已取消），并把对应商品从 1（已预订）恢复为 0（在售）。
 * <p>
 * 两个参数都可以在 application.yml 里改：
 * <ul>
 *   <li>order.timeout-hours：超时小时数，默认 24</li>
 *   <li>order.timeout-cron：扫描频率，默认每 10 分钟（cron 表达式见配置文件）</li>
 * </ul>
 * 本地想快速验证时，可以把 timeout-hours 改成 0、cron 改成每 10 秒。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    /** 超过多少小时未确认就自动取消，默认 24 */
    @Value("${order.timeout-hours:24}")
    private long timeoutHours;

    @Scheduled(cron = "${order.timeout-cron:0 0/10 * * * *}")
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(timeoutHours);

        List<Order> candidates = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, OrderService.STATUS_PENDING_SELLER)
                .lt(Order::getCreateTime, deadline));

        if (candidates.isEmpty()) {
            log.debug("超时订单扫描：没有需要取消的订单（截止时间 {}）", deadline);
            return;
        }

        int canceled = 0;
        for (Order order : candidates) {
            try {
                // 单个订单独立事务：失败不影响其他订单
                if (orderService.cancelTimeoutOrder(order, deadline)) {
                    canceled++;
                    log.info("超时订单已自动取消：orderId={}, itemId={}", order.getId(), order.getItemId());
                }
            } catch (Exception ex) {
                log.error("超时订单取消失败：orderId={}", order.getId(), ex);
            }
        }
        log.info("超时订单扫描完成：候选 {} 笔，实际取消 {} 笔", candidates.size(), canceled);
    }
}
