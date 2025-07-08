package com.example.iotl.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Configuration
@Slf4j
public class WebClientConfig {

    @Bean
    public WebClient naverWebClient(NaverApiConfig cfg) {
        return WebClient.builder()
                .baseUrl("https://openapi.naver.com")
                .defaultHeader("X-Naver-Client-Id", cfg.getClientId())
                .defaultHeader("X-Naver-Client-Secret", cfg.getClientSecret())
                .filter(ExchangeFilterFunction.ofRequestProcessor(request -> {
                    log.info("▶ Request URL : {}", request.url());
                    request.headers().forEach((name, values) ->
                            log.info("▶ Header: {}={}", name, values));
                    return Mono.just(request);
                }))
                .build();
    }
}