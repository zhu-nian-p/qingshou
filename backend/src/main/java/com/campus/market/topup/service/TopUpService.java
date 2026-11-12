package com.campus.market.topup.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.admin.service.AdminService;
import com.campus.market.common.BusinessException;
import com.campus.market.item.entity.Item;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.topup.dto.ApplyTopUpRequest;
import com.campus.market.topup.dto.TopUpOrderVO;
import com.campus.market.topup.entity.TopUpOrder;
import com.campus.market.topup.mapper.TopUpOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 置顶变现：用户申请置顶 -> 管理员审核 -> 商品真正置顶。
 * <p>
 * 状态机：0=待审核 -> 1=已支付/已生效；0 -> 2=已驳回。
 * 审核动作带原状态条件（UPDATE ... WHERE status = 0），重复审核不会重复生效。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopUpService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PAID = 1;
    private static final int STATUS_REJECTED = 2;

    private final TopUpOrderMapper topUpOrderMapper;
    private final ItemMapper itemMapper;
    private final AdminService adminService;

    /** 置顶单价：元/天 */
    @Value("${topup.price-per-day:1.00}")
    private BigDecimal pricePerDay;

    /** 用户申请置顶，生成待审核订单 */
    public TopUpOrderVO apply(Long userId, ApplyTopUpRequest request) {
        Item item = itemMapper.selectById(request.getItemId());
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        if (!item.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("只能给自己发布的商品购买置顶");
        }

        int days = request.getDays();
        BigDecimal price = pricePerDay.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);

        TopUpOrder order = new TopUpOrder();
        order.setItemId(item.getId());
        order.setUserId(userId);
        order.setDays(days);
        order.setPrice(price);
        order.setStatus(STATUS_PENDING);
        topUpOrderMapper.insert(order);

        log.info("用户 {} 申请置顶商品 {}，{} 天，金额 {}", userId, item.getId(), days, price);
        return toVO(topUpOrderMapper.selectById(order.getId()), item);
    }

    /** 我的置顶订单 */
    public List<TopUpOrderVO> myOrders(Long userId) {
        return toVOList(topUpOrderMapper.selectList(new LambdaQueryWrapper<TopUpOrder>()
                .eq(TopUpOrder::getUserId, userId)
                .orderByDesc(TopUpOrder::getCreateTime)));
    }

    /** 管理员：待审核列表 */
    public List<TopUpOrderVO> pendingOrders(Long adminId) {
        adminService.requireAdmin(adminId);
        return toVOList(topUpOrderMapper.selectList(new LambdaQueryWrapper<TopUpOrder>()
                .eq(TopUpOrder::getStatus, STATUS_PENDING)
                .orderByAsc(TopUpOrder::getCreateTime)));
    }

    /** 管理员审核通过：订单置为已支付，商品真正置顶 */
    @Transactional
    public TopUpOrderVO approve(Long adminId, Long topUpId) {
        adminService.requireAdmin(adminId);
        TopUpOrder order = getExisting(topUpId);
        if (order.getStatus() != STATUS_PENDING) {
            throw BusinessException.badRequest("该置顶订单不是「待审核」状态");
        }

        int updated = topUpOrderMapper.approve(topUpId);
        if (updated != 1) {
            throw BusinessException.badRequest("订单状态已变化，请刷新后重试");
        }

        LocalDateTime expireTime = LocalDateTime.now().plusDays(order.getDays());
        itemMapper.updateTop(order.getItemId(), 1, expireTime);
        log.info("管理员 {} 审核通过置顶订单 {}：商品 {} 置顶至 {}", adminId, topUpId, order.getItemId(), expireTime);

        return toVO(topUpOrderMapper.selectById(topUpId), itemMapper.selectById(order.getItemId()));
    }

    /** 管理员驳回 */
    @Transactional
    public TopUpOrderVO reject(Long adminId, Long topUpId) {
        adminService.requireAdmin(adminId);
        TopUpOrder order = getExisting(topUpId);
        if (order.getStatus() != STATUS_PENDING) {
            throw BusinessException.badRequest("该置顶订单不是「待审核」状态");
        }
        int updated = topUpOrderMapper.reject(topUpId);
        if (updated != 1) {
            throw BusinessException.badRequest("订单状态已变化，请刷新后重试");
        }
        return toVO(topUpOrderMapper.selectById(topUpId), itemMapper.selectById(order.getItemId()));
    }

    private TopUpOrder getExisting(Long topUpId) {
        TopUpOrder order = topUpOrderMapper.selectById(topUpId);
        if (order == null) {
            throw BusinessException.notFound("置顶订单不存在");
        }
        return order;
    }

    private List<TopUpOrderVO> toVOList(List<TopUpOrder> orders) {
        if (orders.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> itemIds = orders.stream().map(TopUpOrder::getItemId).distinct().toList();
        Map<Long, Item> itemMap = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                        .in(Item::getId, itemIds))
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        return orders.stream().map(order -> toVO(order, itemMap.get(order.getItemId()))).toList();
    }

    private TopUpOrderVO toVO(TopUpOrder order, Item item) {
        TopUpOrderVO vo = new TopUpOrderVO();
        vo.setId(order.getId());
        vo.setItemId(order.getItemId());
        if (item != null) {
            vo.setItemTitle(item.getTitle());
        }
        vo.setUserId(order.getUserId());
        vo.setDays(order.getDays());
        vo.setPrice(order.getPrice());
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
            case STATUS_PENDING -> "待审核";
            case STATUS_PAID -> "已支付/已生效";
            case STATUS_REJECTED -> "已驳回";
            default -> "未知";
        };
    }
}
