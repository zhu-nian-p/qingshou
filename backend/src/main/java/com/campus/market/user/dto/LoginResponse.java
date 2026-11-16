package com.campus.market.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    /** 后续请求放在请求头 Authorization: Bearer {token} */
    private String token;

    private Long userId;

    /** 昵称，用于前端展示 */
    private String nickname;

    /** 学号，登录账号 */
    private String studentId;

    private String realName;

    /** 0=普通用户，1=管理员，前端可据此显示后台入口 */
    private Integer role;
}
