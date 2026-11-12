package com.campus.market.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.item.entity.Item;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品 Mapper。
 * <p>
 * 下单防超卖的核心就在这里：用带 status 条件的 UPDATE 做乐观锁，
 * 只有受影响行数 = 1 才说明抢到了。
 */
public interface ItemMapper extends BaseMapper<Item> {

    /**
     * 抢占商品：0=在售 -> 1=已预订。
     * 同一件商品被多人同时下单时，只有第一个 UPDATE 能命中 status = 0。
     */
    @Update("UPDATE `item` SET status = 1 WHERE id = #{itemId} AND status = 0")
    int reserve(@Param("itemId") Long itemId);

    /**
     * 交易完成：1=已预订 -> 2=已售出。
     */
    @Update("UPDATE `item` SET status = 2 WHERE id = #{itemId} AND status = 1")
    int markAsSold(@Param("itemId") Long itemId);

    /**
     * 订单取消/卖家拒单后恢复在售：1=已预订 -> 0=在售。
     * 条件限定原状态为 1，避免把已售出的商品错误地改回在售。
     */
    @Update("UPDATE `item` SET status = 0 WHERE id = #{itemId} AND status = 1")
    int release(@Param("itemId") Long itemId);

    /**
     * 管理员置顶 / 取消置顶。
     *
     * @param isTop      1=置顶，0=取消
     * @param expireTime 置顶到期时间；取消置顶时传 null
     */
    @Update("UPDATE `item` SET is_top = #{isTop}, top_expire_time = #{expireTime} WHERE id = #{itemId}")
    int updateTop(@Param("itemId") Long itemId,
                  @Param("isTop") int isTop,
                  @Param("expireTime") java.time.LocalDateTime expireTime);
}
