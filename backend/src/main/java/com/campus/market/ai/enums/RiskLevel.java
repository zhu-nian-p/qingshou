package com.campus.market.ai.enums;

public enum RiskLevel {

    LOW,
    MEDIUM,
    HIGH;

    public static RiskLevel max(RiskLevel left, RiskLevel right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.ordinal() >= right.ordinal() ? left : right;
    }
}
