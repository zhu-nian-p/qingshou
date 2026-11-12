package com.campus.market.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 返回给前端的用户资料，不包含密码。
 */
@Data
public class UserProfileVO {

    private Long id;

    private String username;

    private String realName;

    private String studentId;

    private String schoolEmail;

    private Integer status;

    /** 0=普通用户，1=管理员 */
    private Integer role;

    private Integer creditScore;

    private LocalDateTime createTime;
}
