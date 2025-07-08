package com.example.iotl.service.exchange;

import com.example.iotl.dto.exchange.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ExchangeApiService {

    @Value("${exchange.api.base-url}")
    private String exchangeApiUrl;

    private final WebClient webClient = WebClient.create();

    public ExchangeRateResponse fetch() {
        return webClient.get()
                .uri(exchangeApiUrl)
                .retrieve()
                .bodyToMono(ExchangeRateResponse.class)
                .block();
    }
}