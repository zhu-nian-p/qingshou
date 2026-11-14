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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private static final int ITEM_ON_SALE = 0;

    private final OrderMapper orderMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final ItemListCacheService cacheService;

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
     * 买家确认完成：0=待面交 -> 1=已完成。
     * <p>
     * 严格校验操作人必须是 buyer_id（卖家来点直接 403）；
     * 完成后商品置为 2=已售出，买卖双方信用分各 +1。
     */
    @Transactional
    public OrderVO buyerComplete(Long orderId, Long currentUserId) {
        Order order = getExistingOrder(orderId);
        if (!order.getBuyerId().equals(currentUserId)) {
            throw BusinessException.forbidden("只有买家本人可以确认完成交易");
        }
        if (order.getStatus() != STATUS_WAIT_MEET) {
            throw BusinessException.badRequest("订单当前状态不是「待面交」，无法确认完成");
        }

        // 订单 0 -> 1，带原状态条件，防止重复点击导致信用分加两次
        int updated = orderMapper.completeByBuyer(orderId, currentUserId);
        if (updated != 1) {
            throw BusinessException.badRequest("订单状态已发生变化，请刷新后重试");
        }

        // 商品 1 -> 2
        int itemUpdated = itemMapper.markAsSold(order.getItemId());
        if (itemUpdated != 1) {
            throw BusinessException.badRequest("商品状态异常，无法完成交易");
        }

        // 双方信用分 +1
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
            case STATUS_COMPLETED -> "已完成";
            case STATUS_CANCELED -> "已取消";
            default -> "未知";
        };
    }
}
