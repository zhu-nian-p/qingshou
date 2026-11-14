package com.campus.market.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.report.entity.Report;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ReportMapper extends BaseMapper<Report> {

    /** 0=待处理 -> 1=已处理，带原状态条件防重复处理 */
    @Update("UPDATE `report` SET status = 1, handler_id = #{handlerId}, handle_time = NOW() "
            + "WHERE id = #{id} AND status = 0")
    int markHandled(@Param("id") Long id, @Param("handlerId") Long handlerId);
}
