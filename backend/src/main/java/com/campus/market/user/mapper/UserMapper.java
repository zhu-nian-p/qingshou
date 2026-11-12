package com.campus.market.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.market.user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface UserMapper extends BaseMapper<User> {

    /**
     * 信用分增减。用 SQL 自增而不是「先查再改」，避免并发丢失更新。
     *
     * @param delta 正数加分，负数扣分
     */
    @Update("UPDATE `user` SET credit_score = credit_score + #{delta} WHERE id = #{userId}")
    int addCreditScore(@Param("userId") Long userId, @Param("delta") int delta);

    /**
     * 修改账号状态：0=正常，1=封禁。
     */
    @Update("UPDATE `user` SET status = #{status} WHERE id = #{userId}")
    int updateStatus(@Param("userId") Long userId, @Param("status") int status);
}
