package com.campus.market.ai.dto;

import com.campus.market.ai.enums.RiskCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskCategoryVO {

    private String code;

    private String label;

    public static RiskCategoryVO of(RiskCategory category) {
        return new RiskCategoryVO(category.name(), category.getLabel());
    }
}
