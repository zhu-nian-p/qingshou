package com.campus.market.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词配置：app.sensitive-words
 * 修改 application.yml 或环境变量即可增删，无需改代码重新编译。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class SensitiveWordProperties {

    /** 命中任意一个词就拒绝发布 */
    private List<String> sensitiveWords = new ArrayList<>(List.of("代写", "违禁", "诈骗"));
}
