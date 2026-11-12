package com.campus.market.common;

import java.util.List;

/**
 * 极简敏感词过滤器。
 * <p>
 * 目前只做「包含即拦截」，够校园二手场景的第一版使用。
 * 后续要扩展时可以替换成 DFA 字典树 + 词库文件，接口保持不变即可。
 */
public final class SensitiveWordFilter {

    /** 命中任意一个词就拒绝发布 */
    private static final List<String> SENSITIVE_WORDS = List.of("代写", "违禁", "诈骗");

    private SensitiveWordFilter() {
    }

    /**
     * 校验若干段文本，命中敏感词直接抛业务异常（HTTP 400）。
     *
     * @param texts 待校验文本，null 会被跳过
     */
    public static void check(String... texts) {
        if (texts == null) {
            return;
        }
        for (String text : texts) {
            if (text == null || text.isEmpty()) {
                continue;
            }
            for (String word : SENSITIVE_WORDS) {
                if (text.contains(word)) {
                    throw BusinessException.badRequest("内容包含敏感词：「" + word + "」，请修改后再发布");
                }
            }
        }
    }
}
