package com.campus.market.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtUtil(JwtProperties properties) {
        this.properties = properties;
        String secret = properties.getSecret();
        if (secret == null || secret.isBlank() || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET 未配置或长度不足 32 个字符，请在 .env.local 中设置");
        }
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String createToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(properties.getExpireHours(), ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(key)
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object uid = claims.get("uid");
        if (uid instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
