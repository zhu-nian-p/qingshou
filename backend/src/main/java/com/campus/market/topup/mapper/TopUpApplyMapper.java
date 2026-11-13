package com.campus.market.topup.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.topup.entity.TopUpApply;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 置顶申请 Mapper。
 * 审核动作带原状态条件，避免重复审核导致重复置顶。
 */
public interface TopUpApplyMapper extends BaseMapper<TopUpApply> {

    /** 0=待审核 -> 1=已通过 */
    @Update("UPDATE `top_up_apply` SET status = 1 WHERE id = #{id} AND status = 0")
    int approve(@Param("id") Long id);

    /** 0=待审核 -> 2=已拒绝 */
    @Update("UPDATE `top_up_apply` SET status = 2 WHERE id = #{id} AND status = 0")
    int reject(@Param("id") Long id);
}
