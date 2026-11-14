package com.campus.market.common.ratelimit;

import com.campus.market.common.BusinessException;
import com.campus.market.security.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * @RateLimit 切面：Redis INCR + EXPIRE 实现固定窗口限流。
 * <p>
 * 用 Lua 脚本把「INCR + 首次设置 EXPIRE」做成原子操作，避免并发下 key 永不过期。
 * 限流维度：已登录用户按 userId，未登录按客户端 IP。
 * Redis 异常时选择放行（fail-open），避免缓存故障把业务全部拖垮。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String KEY_PREFIX = "qingshou:rate:";

    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1])\n"
                    + "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end\n"
                    + "return current",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        int windowSeconds = Math.max(1, rateLimit.time());
        int maxCount = Math.max(1, rateLimit.count());

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String identity = currentIdentity();
        String key = KEY_PREFIX + methodName + ":" + identity;

        long current;
        try {
            Long value = redisTemplate.execute(SCRIPT, List.of(key), String.valueOf(windowSeconds));
            current = value == null ? 1L : value;
        } catch (Exception e) {
            log.warn("限流 Redis 异常，本次放行: key={}, err={}", key, e.getMessage());
            return joinPoint.proceed();
        }

        if (current > maxCount) {
            log.warn("触发接口限流: key={}, current={}, limit={}", key, current, maxCount);
            throw new BusinessException(429, "手速太快啦，请稍后再试");
        }

        return joinPoint.proceed();
    }

    /** 已登录按用户，未登录按 IP */
    private String currentIdentity() {
        Long userId = UserContext.get();
        if (userId != null) {
            return "u:" + userId;
        }
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "anonymous";
            }
            HttpServletRequest request = attributes.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return "ip:" + forwarded.split(",")[0].trim();
            }
            return "ip:" + request.getRemoteAddr();
        } catch (Exception e) {
            return "anonymous";
        }
    }
}
