package com.campus.market.topup.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.admin.service.AdminService;
import com.campus.market.common.BusinessException;
import com.campus.market.item.entity.Item;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.item.service.ItemListCacheService;
import com.campus.market.topup.dto.ApplyTopUpRequest;
import com.campus.market.topup.dto.TopUpApplyVO;
import com.campus.market.topup.entity.TopUpApply;
import com.campus.market.topup.mapper.TopUpApplyMapper;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 置顶变现：用户提交申请 -> 管理员人工审核（扫码付款后）-> 商品置顶。
 * <p>
 * 阶梯定价：1 天 1 元，7 天 5 元，30 天 12 元。
 * 金额只在后端计算并落库，前端传什么金额都不参与。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TopUpService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;

    /** 天数 -> 金额（元） */
    private static final Map<Integer, BigDecimal> TIER_PRICE = Map.of(
            1, new BigDecimal("1.00"),
            7, new BigDecimal("5.00"),
            30, new BigDecimal("12.00")
    );

    private final TopUpApplyMapper topUpApplyMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final AdminService adminService;
    private final ItemListCacheService cacheService;

    /** 卖家提交置顶申请，生成待审核记录 */
    public TopUpApplyVO apply(Long userId, ApplyTopUpRequest request) {
        Item item = itemMapper.selectById(request.getItemId());
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        if (!item.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("只能给自己发布的商品申请置顶");
        }

        BigDecimal amount = TIER_PRICE.get(request.getDays());
        if (amount == null) {
            throw BusinessException.badRequest("置顶天数只能是 1 天、7 天或 30 天");
        }

        TopUpApply apply = new TopUpApply();
        apply.setItemId(item.getId());
        apply.setUserId(userId);
        apply.setDays(request.getDays());
        apply.setAmount(amount);
        apply.setStatus(STATUS_PENDING);
        topUpApplyMapper.insert(apply);

        log.info("用户 {} 为商品 {} 提交置顶申请：{} 天，应付 {} 元", userId, item.getId(), apply.getDays(), amount);
        return toVO(topUpApplyMapper.selectById(apply.getId()), item, userMapper.selectById(userId));
    }

    /** 我的置顶申请 */
    public List<TopUpApplyVO> myApplies(Long userId) {
        return toVOList(topUpApplyMapper.selectList(new LambdaQueryWrapper<TopUpApply>()
                .eq(TopUpApply::getUserId, userId)
                .orderByDesc(TopUpApply::getCreateTime)));
    }

    /** 管理员：待审核列表（关联商品名称） */
    public List<TopUpApplyVO> pendingApplies(Long adminId) {
        adminService.requireAdmin(adminId);
        return toVOList(topUpApplyMapper.selectList(new LambdaQueryWrapper<TopUpApply>()
                .eq(TopUpApply::getStatus, STATUS_PENDING)
                .orderByAsc(TopUpApply::getCreateTime)));
    }

    /** 管理员审核通过：状态改 1，商品置顶对应天数 */
    @Transactional
    public TopUpApplyVO approve(Long adminId, Long applyId) {
        adminService.requireAdmin(adminId);
        TopUpApply apply = getExisting(applyId);
        if (apply.getStatus() != STATUS_PENDING) {
            throw BusinessException.badRequest("该置顶申请不是「待审核」状态");
        }

        int updated = topUpApplyMapper.approve(applyId);
        if (updated != 1) {
            throw BusinessException.badRequest("申请状态已变化，请刷新后重试");
        }

        LocalDateTime expireTime = LocalDateTime.now().plusDays(apply.getDays());
        itemMapper.updateTop(apply.getItemId(), 1, expireTime);
        cacheService.evictAll();
        log.info("管理员 {} 通过置顶申请 {}：商品 {} 置顶至 {}", adminId, applyId, apply.getItemId(), expireTime);

        return toVO(topUpApplyMapper.selectById(applyId), itemMapper.selectById(apply.getItemId()),
                userMapper.selectById(apply.getUserId()));
    }

    /** 管理员拒绝：状态改 2，并记录拒绝原因 */
    @Transactional
    public TopUpApplyVO reject(Long adminId, Long applyId, String reason) {
        adminService.requireAdmin(adminId);
        TopUpApply apply = getExisting(applyId);
        if (apply.getStatus() != STATUS_PENDING) {
            throw BusinessException.badRequest("该置顶申请不是「待审核」状态");
        }
        String trimmed = reason == null ? "" : reason.trim();
        if (trimmed.isEmpty()) {
            throw BusinessException.badRequest("拒绝原因不能为空");
        }
        if (trimmed.length() > 255) {
            throw BusinessException.badRequest("拒绝原因不能超过 255 个字符");
        }

        int updated = topUpApplyMapper.reject(applyId, trimmed);
        if (updated != 1) {
            throw BusinessException.badRequest("申请状态已变化，请刷新后重试");
        }
        log.info("管理员 {} 拒绝置顶申请 {}，原因：{}", adminId, applyId, trimmed);
        return toVO(topUpApplyMapper.selectById(applyId), itemMapper.selectById(apply.getItemId()),
                userMapper.selectById(apply.getUserId()));
    }

    private TopUpApply getExisting(Long applyId) {
        TopUpApply apply = topUpApplyMapper.selectById(applyId);
        if (apply == null) {
            throw BusinessException.notFound("置顶申请不存在");
        }
        return apply;
    }

    private List<TopUpApplyVO> toVOList(List<TopUpApply> applies) {
        if (applies.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> itemIds = applies.stream().map(TopUpApply::getItemId).distinct().toList();
        Map<Long, Item> itemMap = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                        .in(Item::getId, itemIds))
                .stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        List<Long> userIds = applies.stream().map(TopUpApply::getUserId).distinct().toList();
        Map<Long, User> userMap = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, userIds))
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return applies.stream()
                .map(apply -> toVO(apply, itemMap.get(apply.getItemId()), userMap.get(apply.getUserId())))
                .toList();
    }

    private TopUpApplyVO toVO(TopUpApply apply, Item item, User user) {
        TopUpApplyVO vo = new TopUpApplyVO();
        vo.setId(apply.getId());
        vo.setItemId(apply.getItemId());
        if (item != null) {
            vo.setItemTitle(item.getTitle());
        }
        vo.setUserId(apply.getUserId());
        if (user != null) {
            String realName = user.getRealName();
            vo.setUserName(realName == null || realName.isBlank()
                    ? user.getUsername()
                    : realName + "（" + user.getUsername() + "）");
        }
        vo.setDays(apply.getDays());
        vo.setAmount(apply.getAmount());
        vo.setStatus(apply.getStatus());
        vo.setStatusText(statusText(apply.getStatus()));
        vo.setRejectReason(apply.getRejectReason());
        vo.setCreateTime(apply.getCreateTime());
        return vo;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case STATUS_PENDING -> "待审核";
            case STATUS_APPROVED -> "已通过";
            case STATUS_REJECTED -> "已拒绝";
            default -> "未知";
        };
    }
}
