package com.campus.market.ai.enums;

import lombok.Getter;

@Getter
public enum RiskCategory {

    PROHIBITED_GOODS("违禁品"),
    FRAUD("诈骗/刷单"),
    GHOSTWRITING("代写代考"),
    COUNTERFEIT("侵权假冒"),
    OFF_PLATFORM("站外交易"),
    OTHER("其他");

    private final String label;

    RiskCategory(String label) {
        this.label = label;
    }

    public static RiskCategory fromKey(String key) {
        if (key == null) {
            return OTHER;
        }
        return switch (key.trim().toLowerCase()) {
            case "prohibited-goods", "prohibited_goods" -> PROHIBITED_GOODS;
            case "fraud" -> FRAUD;
            case "ghostwriting" -> GHOSTWRITING;
            case "counterfeit" -> COUNTERFEIT;
            case "off-platform", "off_platform" -> OFF_PLATFORM;
            default -> OTHER;
        };
    }

    public static RiskCategory fromCode(String code) {
        if (code == null || code.isBlank()) {
            return OTHER;
        }
        String value = code.trim();
        for (RiskCategory category : values()) {
            if (category.name().equalsIgnoreCase(value) || category.label.equals(value)) {
                return category;
            }
        }
        return OTHER;
    }
}
