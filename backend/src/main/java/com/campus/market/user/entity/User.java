package com.campus.market.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对应表 user。表名由 application.yml 的 table-format 自动加上反引号。
 * 时间字段交给数据库默认值和 ON UPDATE，插入时不写这两列。
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String nickname;

    private String password;

    private String realName;

    private String studentId;

    private String schoolEmail;

    /** 0=正常，1=已封禁 */
    private Integer status;

    /** 0=普通用户，1=管理员 */
    private Integer role;

    private Integer creditScore;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;

    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
}
