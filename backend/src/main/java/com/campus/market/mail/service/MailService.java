package com.campus.market.mail.service;

import com.campus.market.common.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;

/**
 * 邮件发送服务。
 * <p>
 * QQ 邮箱 SMTP：smtp.qq.com:465 + SSL，账号/授权码来自环境变量
 * MAIL_USERNAME / MAIL_AUTH_CODE（见 .env.local 与 application.yml）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    @Value("${app.mail.sender-name:青售官方团队}")
    private String senderName;

    /** 发送注册验证码邮件 */
    public void sendRegisterCode(String to, String code) {
        if (!StringUtils.hasText(fromAddress)) {
            throw new BusinessException(503, "邮件服务未配置发件邮箱（MAIL_USERNAME）");
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(new InternetAddress(fromAddress, senderName, "UTF-8"));
            helper.setTo(to);
            helper.setSubject("【青售】您的注册验证码");
            helper.setText(buildContent(code), false);
            mailSender.send(message);
            log.info("注册验证码邮件已发送至 {}", maskEmail(to));
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.warn("构造验证码邮件失败: {}", ex.getMessage());
            throw new BusinessException(502, "验证码邮件发送失败，请稍后重试");
        } catch (RuntimeException ex) {
            log.error("发送验证码邮件异常: {}", maskEmail(to), ex);
            throw new BusinessException(502, "验证码邮件发送失败，请检查邮箱地址或稍后重试");
        }
    }

    private String buildContent(String code) {
        return "同学你好：\n\n"
                + "欢迎使用青售（校园二手交易平台）。\n\n"
                + "您的注册验证码是：" + code + "\n\n"
                + "验证码 5 分钟内有效，请勿转发给他人。\n"
                + "如非本人操作，请忽略本邮件。\n\n"
                + "—— 青售官方团队";
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
