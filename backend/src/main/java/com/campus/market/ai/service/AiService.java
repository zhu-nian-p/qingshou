package com.campus.market.ai.service;

import com.campus.market.ai.dto.AiDescResponse;
import com.campus.market.common.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 文案生成。
 * <p>
 * 走 OpenAI 兼容协议：POST {base-url}/chat/completions。
 * 配置项见 application.yml 的 deepseek.*；未配置 api-key 时直接返回 503 友好提示。
 */
@Slf4j
@Service
public class AiService {

    private static final String SYSTEM_PROMPT = """
            你是校园二手交易平台的文案助手。用户会给你一个商品关键词，
            请写一段约 50 字的中文二手商品描述：说明成色、卖点、适合人群，
            支持校内面交。语气真诚自然，不要标题、不要序号、不要引号。
            """;

    /** 没配 API Key 时返回的模拟文案模板 */
    private static final String MOCK_TEMPLATE =
            "【模拟文案】%s 九成新，功能正常、外观干净，日常使用很爱惜。因毕业搬宿舍低价转让，支持校内面交，价格可小刀，诚心要的优先联系～";

    private final RestTemplate aiRestTemplate;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    public AiService(@Qualifier("aiRestTemplate") RestTemplate aiRestTemplate) {
        this.aiRestTemplate = aiRestTemplate;
    }

    public AiDescResponse generateDescription(String keyword) {
        // 没配 Key 时不让前端报错，直接返回模拟文案，方便先跑通流程
        if (!StringUtils.hasText(apiKey)) {
            String mock = String.format(MOCK_TEMPLATE, keyword.trim());
            log.info("未配置 deepseek.api-key，返回模拟文案：{}", keyword);
            return new AiDescResponse(mock, "mock", true);
        }

        String url = baseUrl.replaceAll("/+$", "") + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", 0.8);
        body.put("max_tokens", 400);
        body.put("stream", false);
        body.put("messages", List.of(
                Map.of("role", "system", "content", SYSTEM_PROMPT),
                Map.of("role", "user", "content", "商品关键词：" + keyword.trim())
        ));

        try {
            JsonNode response = aiRestTemplate.postForObject(url, new HttpEntity<>(body, headers), JsonNode.class);
            String content = response == null
                    ? ""
                    : response.path("choices").path(0).path("message").path("content").asText("");
            if (!StringUtils.hasText(content)) {
                throw new BusinessException(502, "AI 返回内容为空，请换个说法再试");
            }
            return new AiDescResponse(content.trim(), model, false);
        } catch (HttpClientErrorException.Unauthorized ex) {
            log.warn("DeepSeek 鉴权失败: {}", ex.getResponseBodyAsString());
            throw new BusinessException(401, "DeepSeek API Key 无效，请检查配置");
        } catch (HttpClientErrorException ex) {
            log.warn("DeepSeek 返回错误 {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new BusinessException(502, "AI 服务返回错误（" + ex.getStatusCode().value() + "），请稍后重试");
        } catch (ResourceAccessException ex) {
            log.warn("DeepSeek 连接失败: {}", ex.getMessage());
            throw new BusinessException(504, "AI 服务连接超时，请稍后重试");
        } catch (RestClientException ex) {
            log.warn("DeepSeek 调用失败: {}", ex.getMessage());
            throw new BusinessException(502, "AI 服务暂时不可用，请稍后重试");
        }
    }
}
