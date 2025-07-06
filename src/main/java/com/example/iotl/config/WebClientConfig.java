package com.example.iotl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
@Slf4j
public class WebClientConfig {
    @Bean
    public WebClient naverWebClient(NaverApiConfig naverApiConfig) {
        log.info("Naver clientId='{}', clientSecret='{}'", naverApiConfig.getClientId(), naverApiConfig.getClientSecret());
        return WebClient.builder()
                .baseUrl("https://openapi.naver.com")
                .defaultHeader("X-Naver-Client-Id", naverApiConfig.getClientId())
                .defaultHeader("X-Naver-Client-Secret", naverApiConfig.getClientSecret())
                .build();
    }
}