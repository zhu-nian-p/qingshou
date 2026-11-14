package com.campus.market.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 敏感词过滤器（词库外置到 application.yml 的 app.sensitive-words）。
 * <p>
 * 当前为「包含即拦截」；后续可替换为 DFA 字典树，接口保持不变。
 */
@Component
@RequiredArgsConstructor
public class SensitiveWordFilter {

    private final SensitiveWordProperties properties;

    /**
     * 校验若干段文本，命中敏感词直接抛业务异常（HTTP 400）。
     *
     * @param texts 待校验文本，null 会被跳过
     */
    public void check(String... texts) {
        if (texts == null) {
            return;
        }
        List<String> words = properties.getSensitiveWords();
        if (words == null || words.isEmpty()) {
            return;
        }
        for (String text : texts) {
            if (text == null || text.isEmpty()) {
                continue;
            }
            for (String word : words) {
                if (word != null && !word.isBlank() && text.contains(word)) {
                    throw BusinessException.badRequest("内容包含敏感词：「" + word + "」，请修改后再发布");
                }
            }
        }
    }
}
