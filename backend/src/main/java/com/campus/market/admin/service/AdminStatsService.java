package com.campus.market.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.admin.dto.AdminStatsVO;
import com.campus.market.item.entity.Item;
import com.campus.market.item.mapper.ItemMapper;
import com.campus.market.order.mapper.OrderMapper;
import com.campus.market.user.entity.User;
import com.campus.market.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台数据看板：用户、商品、交易流水与发布趋势。
 */
@Service
@RequiredArgsConstructor
public class AdminStatsService {

    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final OrderMapper orderMapper;
    private final AdminService adminService;

    public AdminStatsVO stats(Long adminId) {
        adminService.requireAdmin(adminId);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();

        long totalUsers = userMapper.selectCount(null);
        long todayNewUsers = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .ge(User::getCreateTime, startOfToday));

        long onSaleItems = itemMapper.selectCount(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, 0));
        long todayPublishedItems = itemMapper.selectCount(new LambdaQueryWrapper<Item>()
                .ge(Item::getCreateTime, startOfToday));

        BigDecimal completedAmount = orderMapper.sumCompletedAmount();
        if (completedAmount == null) {
            completedAmount = BigDecimal.ZERO;
        }

        List<Item> recentItems = itemMapper.selectList(new LambdaQueryWrapper<Item>()
                .ge(Item::getCreateTime, today.minusDays(6).atStartOfDay())
                .select(Item::getId, Item::getCreateTime));
        Map<LocalDate, Long> countByDay = recentItems.stream()
                .filter(item -> item.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getCreateTime().toLocalDate(),
                        Collectors.counting()));

        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern("MM-dd");
        List<AdminStatsVO.TrendPoint> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long count = countByDay.getOrDefault(day, 0L);
            trend.add(new AdminStatsVO.TrendPoint(day.toString(), day.format(labelFormat), count));
        }

        AdminStatsVO vo = new AdminStatsVO();
        vo.setTotalUsers(totalUsers);
        vo.setTodayNewUsers(todayNewUsers);
        vo.setOnSaleItems(onSaleItems);
        vo.setTodayPublishedItems(todayPublishedItems);
        vo.setCompletedAmount(completedAmount);
        vo.setTrend(trend);
        return vo;
    }
}
