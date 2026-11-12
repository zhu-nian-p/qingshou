package com.campus.market.ai.controller;

import com.campus.market.ai.dto.AiDescRequest;
import com.campus.market.ai.dto.AiDescResponse;
import com.campus.market.ai.service.AiService;
import com.campus.market.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 能力：根据关键词生成二手商品文案。
 * 需要登录（/api/** 统一鉴权）。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/generate-desc")
    public Result<AiDescResponse> generateDesc(@Valid @RequestBody AiDescRequest request) {
        return Result.success("青售提示：生成成功", aiService.generateDescription(request.getKeyword()));
    }
}
