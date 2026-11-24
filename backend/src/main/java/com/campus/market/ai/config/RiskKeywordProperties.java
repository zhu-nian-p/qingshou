package com.campus.market.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地风险关键词：app.risk-keywords。
 * high 命中阻断，medium 命中只警告；与 app.sensitive-words 相互独立。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.risk-keywords")
public class RiskKeywordProperties {

    private Map<String, List<String>> high = new LinkedHashMap<>();

    private Map<String, List<String>> medium = new LinkedHashMap<>();
}
