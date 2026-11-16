package com.campus.market.user.service;

import com.campus.market.common.BusinessException;
import com.campus.market.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * 学校邮箱验证码：发送（限流 + 5 分钟有效）与校验（一次性消费）。
 * <p>
 * Redis key：
 * <ul>
 *   <li>注册：qingshou:email:code:{email} / qingshou:email:limit:{email}</li>
 *   <li>重置密码：qingshou:email:reset:code:{email} / qingshou:email:reset:limit:{email}</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailCodeService {

    private static final String CODE_KEY_PREFIX = "qingshou:email:code:";
    private static final String LIMIT_KEY_PREFIX = "qingshou:email:limit:";
    private static final String RESET_CODE_KEY_PREFIX = "qingshou:email:reset:code:";
    private static final String RESET_LIMIT_KEY_PREFIX = "qingshou:email:reset:limit:";

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration SEND_INTERVAL = Duration.ofMinutes(1);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;

    /** 发送注册验证码：同一邮箱 1 分钟内只能发一次 */
    public void sendCode(String rawEmail) {
        send(rawEmail, CODE_KEY_PREFIX, LIMIT_KEY_PREFIX, mailService::sendRegisterCode);
    }

    /** 发送密码重置验证码 */
    public void sendResetCode(String rawEmail) {
        send(rawEmail, RESET_CODE_KEY_PREFIX, RESET_LIMIT_KEY_PREFIX, mailService::sendResetCode);
    }

    /** 校验并消费注册验证码：匹配后立刻删除，防止重复使用 */
    public void verifyAndConsume(String rawEmail, String inputCode) {
        verify(CODE_KEY_PREFIX, rawEmail, inputCode);
    }

    /** 校验并消费密码重置验证码 */
    public void verifyAndConsumeReset(String rawEmail, String inputCode) {
        verify(RESET_CODE_KEY_PREFIX, rawEmail, inputCode);
    }

    private void send(String rawEmail, String codePrefix, String limitPrefix,
                      BiConsumer<String, String> sender) {
        String email = normalize(rawEmail);
        if (!email.matches("^[a-z0-9._%+-]+@stu\\.nchu\\.edu\\.cn$")) {
            throw BusinessException.badRequest("仅支持 @stu.nchu.edu.cn 学校邮箱");
        }

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(limitPrefix + email, "1", SEND_INTERVAL);
        if (!Boolean.TRUE.equals(acquired)) {
            throw new BusinessException(429, "发送太频繁，请 1 分钟后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        redisTemplate.opsForValue().set(codePrefix + email, code, CODE_TTL);

        try {
            sender.accept(email, code);
        } catch (RuntimeException ex) {
            // 邮件没发出去：回滚限流与验证码，允许用户立即重试
            redisTemplate.delete(codePrefix + email);
            redisTemplate.delete(limitPrefix + email);
            throw ex;
        }
        log.info("邮箱验证码已生成并发送: {}", maskEmail(email));
    }

    private void verify(String codePrefix, String rawEmail, String inputCode) {
        String email = normalize(rawEmail);
        String key = codePrefix + email;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null || !cached.equals(inputCode == null ? "" : inputCode.trim())) {
            throw BusinessException.badRequest("验证码错误或已过期");
        }
        redisTemplate.delete(key);
    }

    private String normalize(String email) {
        if (!StringUtils.hasText(email)) {
            throw BusinessException.badRequest("邮箱不能为空");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
