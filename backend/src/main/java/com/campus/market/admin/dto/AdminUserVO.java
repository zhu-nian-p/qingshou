package com.campus.market.admin.dto;

import lombok.Data;

/** 管理员操作用户后返回的状态信息（不包含密码） */
@Data
public class AdminUserVO {

    private Long id;

    private String nickname;

    private String realName;

    private String studentId;

    private String schoolEmail;

    /** 0=正常，1=已封禁 */
    private Integer status;

    /** 1=普通用户，2=管理员 */
    private Integer role;

    private Integer creditScore;
}
