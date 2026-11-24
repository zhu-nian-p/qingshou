package com.campus.market.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * AI 调用专用 RestTemplate。
 * 用 JDK HttpClient 作为底层实现，连接超时 5 秒、读取超时 30 秒。
 */
@Configuration
public class RestTemplateConfig {

    @Bean("aiRestTemplate")
    public RestTemplate aiRestTemplate() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(30));
        return new RestTemplate(requestFactory);
    }

    /** 风险审核专用：短超时，失败后由本地关键词兜底 */
    @Bean("aiRiskRestTemplate")
    public RestTemplate aiRiskRestTemplate(
            @Value("${ai.risk.connect-timeout-seconds:2}") long connectTimeoutSeconds,
            @Value("${ai.risk.read-timeout-seconds:4}") long readTimeoutSeconds) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
        return new RestTemplate(requestFactory);
    }
}
