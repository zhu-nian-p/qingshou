package com.campus.market.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.order.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * 订单 Mapper。
 * <p>
 * order 是 MySQL 保留字，手写 SQL 一律用反引号 `order`。
 * 状态流转全部用「带原状态条件的 UPDATE」实现乐观锁，避免并发重复流转。
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 卖家确认交易：3=待卖家确认 -> 0=待面交。
     * 条件里带上 seller_id 和原状态，既防越权也防并发重复确认。
     */
    @Update("UPDATE `order` SET status = 0 "
            + "WHERE id = #{orderId} AND seller_id = #{sellerId} AND status = 3")
    int confirmBySeller(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);

    /**
     * 买家确认面交：0=待面交 -> 4=待卖家确认完成。
     * 只改订单状态，不改商品、不加信用分，等卖家确认后才真正完成。
     */
    @Update("UPDATE `order` SET status = 4 "
            + "WHERE id = #{orderId} AND buyer_id = #{buyerId} AND status = 0")
    int buyerConfirmMeet(@Param("orderId") Long orderId, @Param("buyerId") Long buyerId);

    /**
     * 卖家确认完成：4=待卖家确认完成 -> 1=已完成。
     */
    @Update("UPDATE `order` SET status = 1 "
            + "WHERE id = #{orderId} AND seller_id = #{sellerId} AND status = 4")
    int sellerConfirmComplete(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);

    /**
     * 卖家拒绝接单：3=待卖家确认 或 0=待面交 -> 2=已取消。
     * 已完成的订单不允许拒绝，所以条件里限定原状态只能 3 或 0。
     */
    @Update("UPDATE `order` SET status = 2 "
            + "WHERE id = #{orderId} AND seller_id = #{sellerId} AND status IN (3, 0)")
    int rejectBySeller(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);

    /**
     * 超时自动取消：仅处理「待卖家确认」且已超过截止时间的订单。
     * 条件里再校验一次状态和时间，防止扫描后、更新前卖家刚好确认了订单。
     */
    @Update("UPDATE `order` SET status = 2 "
            + "WHERE id = #{orderId} AND status = 3 AND create_time < #{deadline}")
    int cancelIfTimeout(@Param("orderId") Long orderId, @Param("deadline") java.time.LocalDateTime deadline);

    /**
     * 买家主动取消：待卖家确认(3) 或 待面交(0) -> 2=已取消。
     */
    @Update("UPDATE `order` SET status = 2 "
            + "WHERE id = #{orderId} AND buyer_id = #{buyerId} AND status IN (3, 0)")
    int cancelByBuyer(@Param("orderId") Long orderId, @Param("buyerId") Long buyerId);

    /**
     * 待面交超过 N 天自动取消：仅处理 status=0 且最后更新时间早于截止时间。
     */
    @Update("UPDATE `order` SET status = 2 "
            + "WHERE id = #{orderId} AND status = 0 AND update_time < #{deadline}")
    int cancelMeetIfTimeout(@Param("orderId") Long orderId,
                            @Param("deadline") java.time.LocalDateTime deadline);

    /**
     * 已完成订单的总交易流水：关联 item 取商品成交价求和。
     */
    @Select("SELECT IFNULL(SUM(i.price), 0) FROM `order` o "
            + "JOIN `item` i ON i.id = o.item_id WHERE o.status = 1")
    BigDecimal sumCompletedAmount();
}
