package com.campus.market.ai.service;

import com.campus.market.ai.config.RiskKeywordProperties;
import com.campus.market.ai.dto.AiRiskResponse;
import com.campus.market.ai.dto.RiskCategoryVO;
import com.campus.market.ai.enums.RiskCategory;
import com.campus.market.ai.enums.RiskLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 本地风险关键词匹配。命中 high 立即 HIGH，命中 medium 为 MEDIUM。
 * 匹配前会去掉空白和常见分隔符，识别简单的“代 写”“刷-单”规避写法。
 */
@Component
@RequiredArgsConstructor
public class RiskKeywordMatcher {

    private static final int MAX_ITEMS = 3;

    private static final int MAX_TEXT_LENGTH = 80;

    private static final String NORMALIZE_REGEX =
            "[\\s\\u3000\\-_·•.,，。!！?？、;；:：/\\\\|*#@()（）\\[\\]【】\"'“”‘’]+";

    private final RiskKeywordProperties properties;

    public AiRiskResponse match(String title, String description, String category) {
        String text = join(title, description, category);
        String normalized = normalize(text);

        AiRiskResponse result = new AiRiskResponse();
        result.setSource("rule");

        List<RiskCategory> highCategories = new ArrayList<>();
        List<String> highHits = collectHits(properties.getHigh(), text, normalized, highCategories);
        if (!highHits.isEmpty()) {
            result.setLevel(RiskLevel.HIGH);
            addCategories(result, highCategories);
            addText(result.getReasons(),
                    "标题或描述中出现高风险表述：" + joinHits(highHits));
            addText(result.getSuggestions(), suggestionFor(firstCategory(highCategories)));
            return result;
        }

        List<RiskCategory> mediumCategories = new ArrayList<>();
        List<String> mediumHits = collectHits(properties.getMedium(), text, normalized, mediumCategories);
        if (!mediumHits.isEmpty()) {
            result.setLevel(RiskLevel.MEDIUM);
            addCategories(result, mediumCategories);
            addText(result.getReasons(),
                    "检测到需留意表述：" + joinHits(mediumHits));
            addText(result.getSuggestions(), suggestionFor(firstCategory(mediumCategories)));
            return result;
        }

        result.setLevel(RiskLevel.LOW);
        return result;
    }

    private List<String> collectHits(Map<String, List<String>> wordsByCategory,
                                     String text,
                                     String normalized,
                                     List<RiskCategory> matchedCategories) {
        List<String> hits = new ArrayList<>();
        if (wordsByCategory == null || wordsByCategory.isEmpty()) {
            return hits;
        }
        Set<String> seen = new LinkedHashSet<>();
        for (Map.Entry<String, List<String>> entry : wordsByCategory.entrySet()) {
            List<String> words = entry.getValue();
            if (words == null || words.isEmpty()) {
                continue;
            }
            boolean categoryMatched = false;
            for (String word : words) {
                if (!StringUtils.hasText(word)) {
                    continue;
                }
                String keyword = word.trim();
                if (text.contains(keyword) || normalized.contains(normalize(keyword))) {
                    categoryMatched = true;
                    if (seen.add(keyword)) {
                        hits.add(keyword);
                    }
                }
            }
            if (categoryMatched) {
                matchedCategories.add(RiskCategory.fromKey(entry.getKey()));
            }
        }
        return hits;
    }

    private void addCategories(AiRiskResponse result, List<RiskCategory> categories) {
        Set<String> existing = new LinkedHashSet<>();
        for (RiskCategoryVO vo : result.getCategories()) {
            existing.add(vo.getCode());
        }
        for (RiskCategory category : categories) {
            if (existing.add(category.name())) {
                result.getCategories().add(RiskCategoryVO.of(category));
            }
        }
    }

    private RiskCategory firstCategory(List<RiskCategory> categories) {
        return categories.isEmpty() ? RiskCategory.OTHER : categories.get(0);
    }

    private String suggestionFor(RiskCategory category) {
        return switch (category) {
            case PROHIBITED_GOODS -> "校园平台仅允许合法二手闲置交易，请删除违禁品相关内容";
            case FRAUD -> "请走平台交易流程，避免站外转账、先交定金等表述";
            case GHOSTWRITING -> "平台不支持代写、代考、包过等学术不端服务，请删除相关内容";
            case COUNTERFEIT -> "请勿发布高仿、复刻、假冒等侵权商品";
            case OFF_PLATFORM -> "建议使用平台内沟通与交易，避免引导站外付款";
            default -> "请修改相关内容后再发布";
        };
    }

    private String joinHits(List<String> hits) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (String hit : hits) {
            if (!StringUtils.hasText(hit)) {
                continue;
            }
            if (count >= MAX_ITEMS) {
                break;
            }
            builder.append('「').append(hit.trim()).append('」');
            count++;
        }
        return builder.toString();
    }

    private void addText(List<String> target, String text) {
        if (!StringUtils.hasText(text) || target.size() >= MAX_ITEMS) {
            return;
        }
        String value = text.trim().replaceAll("\\s+", " ");
        if (value.length() > MAX_TEXT_LENGTH) {
            value = value.substring(0, MAX_TEXT_LENGTH - 1) + "…";
        }
        if (!target.contains(value)) {
            target.add(value);
        }
    }

    private String join(String title, String description, String category) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.hasText(title)) {
            builder.append(title).append('\n');
        }
        if (StringUtils.hasText(category)) {
            builder.append(category).append('\n');
        }
        if (StringUtils.hasText(description)) {
            builder.append(description);
        }
        return builder.toString();
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT).replaceAll(NORMALIZE_REGEX, "");
    }
}
