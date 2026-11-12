package com.campus.market.topup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.topup.entity.TopUpOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 置顶订单 Mapper。
 * 审核动作带原状态条件，避免重复审核导致重复置顶。
 */
public interface TopUpOrderMapper extends BaseMapper<TopUpOrder> {

    /** 0=待审核 -> 1=已支付/已生效 */
    @Update("UPDATE `top_up_order` SET status = 1 WHERE id = #{id} AND status = 0")
    int approve(@Param("id") Long id);

    /** 0=待审核 -> 2=已驳回 */
    @Update("UPDATE `top_up_order` SET status = 2 WHERE id = #{id} AND status = 0")
    int reject(@Param("id") Long id);
}
