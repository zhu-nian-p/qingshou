package com.campus.market.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/** 管理后台数据看板 */
@Data
public class AdminStatsVO {

    /** 总用户数 */
    private long totalUsers;

    /** 今日新增用户数 */
    private long todayNewUsers;

    /** 在售商品数 */
    private long onSaleItems;

    /** 今日发布商品数 */
    private long todayPublishedItems;

    /** 已完成订单的总交易流水（元） */
    private BigDecimal completedAmount;

    /** 近 7 天发布商品趋势（含今天，按日期升序） */
    private List<TrendPoint> trend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        /** yyyy-MM-dd */
        private String date;
        /** 展示标签 MM-dd */
        private String label;
        private long count;

        public TrendPoint(String date, long count) {
            this.date = date;
            this.count = count;
        }
    }
}
