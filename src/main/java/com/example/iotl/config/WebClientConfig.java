package com.example.iotl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class WebClientConfig {

    @Bean
    public WebClient naverWebClient(NaverApiConfig naverApiConfig) {
        return WebClient.builder()
                .baseUrl("https://openapi.naver.com")
                .defaultHeader("X-Naver-Client-Id", naverApiConfig.getClientId())
                .defaultHeader("X-Naver-Client-Secret", naverApiConfig.getClientSecret())
                .build();
    }
}