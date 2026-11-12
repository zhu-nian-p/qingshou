package com.campus.market.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HS256 密钥，长度至少 32 字节。 */
    private String secret;

    /** token 有效期，单位：小时。 */
    private int expireHours = 24;
}
