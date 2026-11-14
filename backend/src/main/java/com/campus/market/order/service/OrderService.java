package com.campus.market.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.BusinessException;
import com.campus.market.item.entity.Item;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.item.service.ItemListCacheService;
import com.campus.market.order.dto.CreateOrderRequest;
import com.campus.market.order.dto.OrderVO;
import com.campus.market.order.entity.Order;
import com.campus.market.order.mapper.OrderMapper;
import com.campus.market.user.mapper.UserMapper;
import com.campus.market.websocket.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    /** 待面交 */
    public static final int STATUS_WAIT_MEET = 0;
    /** 已完成 */
    public static final int STATUS_COMPLETED = 1;
    /** 已取消 */
    public static final int STATUS_CANCELED = 2;
    /** 待卖家确认（下单后的初始状态） */
    public static final int STATUS_PENDING_SELLER = 3;
    /** 买家已确认面交，待卖家确认完成 */
    public static final int STATUS_PENDING_SELLER_CONFIRM = 4;

    private static final int ITEM_ON_SALE = 0;

    /** 同一买家最多同时进行中的订单数（待卖家确认/待面交/待卖家确认完成） */
    private static final int MAX_ACTIVE_ORDERS = 3;

    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final ItemListCacheService cacheService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 买家下单。
     * <p>
     * 防超卖核心：先把商品从 0(在售) 用 UPDATE ... WHERE status = 0 抢成 1(已预订)，
     * 只有影响行数为 1 才继续插入订单。两个人同时下单时，数据库行锁保证只有一个人成功。
     */
    @Transactional
    public OrderVO create(Long buyerId, CreateOrderRequest request) {
        Item item = itemMapper.selectById(request.getItemId());
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        if (item.getStatus() == null || item.getStatus() != ITEM_ON_SALE) {
            throw BusinessException.badRequest("商品当前不可购买（可能已被预订或已售出）");
        }
        if (item.getSellerId().equals(buyerId)) {
            throw BusinessException.badRequest("不能购买自己发布的商品");
        }

        // 防恶意占单：同时进行中的订单最多 3 笔
        long activeOrders = orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, buyerId)
                .in(Order::getStatus, STATUS_PENDING_SELLER, STATUS_WAIT_MEET, STATUS_PENDING_SELLER_CONFIRM));
        if (activeOrders >= MAX_ACTIVE_ORDERS) {
            throw BusinessException.badRequest("你同时进行中的订单最多 3 笔，请先完成或取消一笔再下单");
        }

        int reserved = itemMapper.reserve(item.getId());
        if (reserved != 1) {
            // 行数不为 1，说明在「查」和「改」之间被别人抢走了
            throw BusinessException.badRequest("手慢了，这件商品已被其他同学抢先下单");
        }

        Order order = new Order();
        order.setItemId(item.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(item.getSellerId());
        order.setTradeLocation(request.getTradeLocation().trim());
        order.setStatus(STATUS_PENDING_SELLER);
        orderMapper.insert(order);
        cacheService.evictAll();
        // 事务提交后（AFTER_COMMIT）推送给卖家，避免回滚误通知
        eventPublisher.publishEvent(new OrderCreatedEvent(
                item.getSellerId(), order.getId(), item.getId(), item.getTitle()));

        return toVO(orderMapper.selectById(order.getId()), item);
    }

    /**
     * 卖家确认交易：3=待卖家确认 -> 0=待面交。
     * 只有订单所属卖家能操作。
     */
    @Transactional
    public OrderVO sellerConfirm(Long orderId, Long currentUserId) {
        Order order = getExistingOrder(orderId);
        if (!order.getSellerId().equals(currentUserId)) {
            throw BusinessException.forbidden("只有卖家本人可以确认这笔订单");
        }
        if (order.getStatus() != STATUS_PENDING_SELLER) {
            throw BusinessException.badRequest("订单当前状态不是「待卖家确认」，无需重复确认");
        }

        int updated = orderMapper.confirmBySeller(orderId, currentUserId);
        if (updated != 1) {
            throw BusinessException.badRequest("订单状态已发生变化，请刷新后重试");
        }
        return toVO(orderMapper.selectById(orderId), itemMapper.selectById(order.getItemId()));
    }

    /**
     * 买家确认面交：0=待面交 -> 4=待卖家确认完成。
     * <p>
     * 只记录买家确认，不直接完成交易、不改商品状态、不加信用分，
     * 必须等卖家在「我卖出的」里确认完成，双方各 +1 信用分才生效。
     */
    @Transactional
    public OrderVO buyerComplete(Long orderId, Long currentUserId) {
        Order order = getExistingOrder(orderId);
        if (!order.getBuyerId().equals(currentUserId)) {
            throw BusinessException.forbidden("只有买家本人可以确认面交");
        }
        if (order.getStatus() != STATUS_WAIT_MEET) {
            throw BusinessException.badRequest("订单当前状态不是「待面交」，无法确认");
        }

        int updated = orderMapper.buyerConfirmMeet(orderId, currentUserId);
        if (updated != 1) {
            throw BusinessException.badRequest("订单状态已发生变化，请刷新后重试");
        }
        return toVO(orderMapper.selectById(orderId), itemMapper.selectById(order.getItemId()));
    }

    /**
     * 卖家确认完成：4=待卖家确认完成 -> 1=已完成。
     * 此时才把商品置为已售出，并给买卖双方各 +1 信用分。
     */
    @Transactional
    public OrderVO sellerComplete(Long orderId, Long currentUserId) {
        Order order = getExistingOrder(orderId);
        if (!order.getSellerId().equals(currentUserId)) {
            throw BusinessException.forbidden("只有卖家本人可以确认完成交易");
        }
        if (order.getStatus() != STATUS_PENDING_SELLER_CONFIRM) {
            throw BusinessException.badRequest("买家还未确认面交，暂时不能完成交易");
        }

        int updated = orderMapper.sellerConfirmComplete(orderId, currentUserId);
        if (updated != 1) {
            throw BusinessException.badRequest("订单状态已发生变化，请刷新后重试");
        }

        int itemUpdated = itemMapper.markAsSold(order.getItemId());
        if (itemUpdated != 1) {
            throw BusinessException.badRequest("商品状态异常，无法完成交易");
        }

        userMapper.addCreditScore(order.getBuyerId(), 1);
        userMapper.addCreditScore(order.getSellerId(), 1);
        cacheService.evictAll();

        return toVO(orderMapper.selectById(orderId), itemMapper.selectById(order.getItemId()));
    }

    /**
     * 卖家拒绝接单：3=待卖家确认 或 0=待面交 -> 2=已取消；
     * 同时把商品从 1=已预订 恢复成 0=在售，让其他同学可以继续买。
     */
    @Transactional
    public OrderVO sellerReject(Long orderId, Long currentUserId) {
        Order order = getExistingOrder(orderId);
        if (!order.getSellerId().equals(currentUserId)) {
            throw BusinessException.forbidden("只有卖家本人可以拒绝这笔订单");
        }
        if (order.getStatus() != STATUS_PENDING_SELLER && order.getStatus() != STATUS_WAIT_MEET) {
            throw BusinessException.badRequest("订单当前状态不能拒绝（已完成或已取消）");
        }

        // 订单 -> 2，带原状态条件，防止并发下重复操作
        int updated = orderMapper.rejectBySeller(orderId, currentUserId);
        if (updated != 1) {
            throw BusinessException.badRequest("订单状态已发生变化，请刷新后重试");
        }

        // 商品 1 -> 0，否则这件商品会一直卡在「已预订」
        int released = itemMapper.release(order.getItemId());
        if (released != 1) {
            throw BusinessException.badRequest("商品状态异常，无法恢复在售");
        }
        cacheService.evictAll();

        return toVO(orderMapper.selectById(orderId), itemMapper.selectById(order.getItemId()));
    }

    /**
     * 买家主动取消：3 或 0 -> 2，并把商品恢复在售。
     */
    @Transactional
    public OrderVO buyerCancel(Long orderId, Long currentUserId) {
        Order order = getExistingOrder(orderId);
        if (!order.getBuyerId().equals(currentUserId)) {
            throw BusinessException.forbidden("只有买家本人可以取消订单");
        }
        if (order.getStatus() != STATUS_PENDING_SELLER && order.getStatus() != STATUS_WAIT_MEET) {
            throw BusinessException.badRequest("当前状态不能取消（已确认完成或已取消）");
        }

        int updated = orderMapper.cancelByBuyer(orderId, currentUserId);
        if (updated != 1) {
            throw BusinessException.badRequest("订单状态已变化，请刷新后重试");
        }
        int released = itemMapper.release(order.getItemId());
        if (released != 1) {
            log.warn("取消订单 {} 成功，但商品 {} 未能恢复在售", orderId, order.getItemId());
        }
        cacheService.evictAll();
        return toVO(orderMapper.selectById(orderId), itemMapper.selectById(order.getItemId()));
    }

    /**
     * 定时任务调用：待面交超过 N 天自动取消，并恢复商品在售。
     */
    @Transactional
    public boolean cancelMeetTimeout(Order order, LocalDateTime deadline) {
        int updated = orderMapper.cancelMeetIfTimeout(order.getId(), deadline);
        if (updated != 1) {
            return false;
        }
        int released = itemMapper.release(order.getItemId());
        if (released != 1) {
            log.warn("待面交超时订单 {} 已取消，但商品 {} 未能恢复在售", order.getId(), order.getItemId());
        }
        cacheService.evictAll();
        return true;
    }

    /**
     * 定时任务调用：取消超时未确认的订单，并把商品恢复为在售。
     * 每个订单独立事务，返回 true 表示本次确实取消了。
     */
    @Transactional
    public boolean cancelTimeoutOrder(Order order, LocalDateTime deadline) {
        int updated = orderMapper.cancelIfTimeout(order.getId(), deadline);
        if (updated != 1) {
            // 扫描之后卖家刚好确认/拒绝了，跳过即可
            return false;
        }
        int released = itemMapper.release(order.getItemId());
        if (released != 1) {
            // 商品可能已被其他流程处理，订单取消仍然生效，记录日志即可
            log.warn("超时取消订单 {} 成功，但商品 {} 未能恢复在售", order.getId(), order.getItemId());
        } else {
            cacheService.evictAll();
        }
        return true;
    }

    /** 买家查询自己下的订单 */
    public List<OrderVO> listByBuyer(Long buyerId) {
        return toVOList(orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, buyerId)
                .orderByDesc(Order::getCreateTime)));
    }

    /** 卖家查询自己卖出的订单 */
    public List<OrderVO> listBySeller(Long sellerId) {
        return toVOList(orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, sellerId)
                .orderByDesc(Order::getCreateTime)));
    }

    private Order getExistingOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        return order;
    }

    private List<OrderVO> toVOList(List<Order> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> itemIds = orders.stream().map(Order::getItemId).distinct().toList();
        Map<Long, Item> itemMap = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                        .in(Item::getId, itemIds))
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        return orders.stream().map(order -> toVO(order, itemMap.get(order.getItemId()))).toList();
    }

    private OrderVO toVO(Order order, Item item) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setItemId(order.getItemId());
        if (item != null) {
            vo.setItemTitle(item.getTitle());
            vo.setItemPrice(item.getPrice());
            vo.setItemImageUrl(item.getImageUrl());
        }
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setTradeLocation(order.getTradeLocation());
        vo.setStatus(order.getStatus());
        vo.setStatusText(statusText(order.getStatus()));
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case STATUS_PENDING_SELLER -> "待卖家确认";
            case STATUS_WAIT_MEET -> "待面交";
            case STATUS_PENDING_SELLER_CONFIRM -> "待卖家确认完成";
            case STATUS_COMPLETED -> "已完成";
            case STATUS_CANCELED -> "已取消";
            default -> "未知";
        };
    }
}
