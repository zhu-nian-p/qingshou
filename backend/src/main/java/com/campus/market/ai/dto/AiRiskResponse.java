package com.campus.market.ai.dto;

import com.campus.market.ai.enums.RiskLevel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** AI 风险审核结果。预检接口和发布/编辑内部审核共用。 */
@Data
public class AiRiskResponse {

    private RiskLevel riskLevel = RiskLevel.LOW;

    private boolean blocked;

    private List<RiskCategoryVO> categories = new ArrayList<>();

    private List<String> reasons = new ArrayList<>();

    private List<String> suggestions = new ArrayList<>();

    /** rule=仅本地规则；hybrid=本地规则与 AI 合并 */
    private String source = "rule";

    private boolean degraded;

    private String notice;

    private String model;

    public void setLevel(RiskLevel level) {
        this.riskLevel = level == null ? RiskLevel.LOW : level;
        this.blocked = this.riskLevel == RiskLevel.HIGH;
    }
}
