package com.campus.market.common.ratelimit;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口限流：在 time 秒内，同一用户/IP 最多允许 count 次。
 * 例：@RateLimit(time = 60, count = 5) 表示每分钟最多 5 次。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 时间窗口（秒） */
    int time() default 60;

    /** 窗口内最大请求次数 */
    int count() default 10;
}
