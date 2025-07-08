package com.example.iotl.service;

import com.example.iotl.dto.exchange.ExchangeRateResponse;
import com.example.iotl.dto.exchange.ExchangeSummaryDto;
import com.example.iotl.entity.Exchange;
import com.example.iotl.repository.ExchangeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    @Value("${exchange.api.base-url}")
    private String exchangeApiUrl;

    private final WebClient webClient = WebClient.create();
    private final ExchangeRepository exchangeRepository;

    // ✅ 외부 API 호출
    public ExchangeRateResponse fetchFromApi() {
        return webClient.get()
                .uri(exchangeApiUrl)
                .retrieve()
                .bodyToMono(ExchangeRateResponse.class)
                .block();
    }

    // ✅ 환율 저장
    public boolean existsByDate(LocalDate date) {
        return exchangeRepository.existsByBaseCodeAndTargetCodeAndDate("USD", "KRW", date);
    }

    public void saveRate(Double rate, LocalDate date) {
        exchangeRepository.save(Exchange.builder()
                .baseCode("USD")
                .targetCode("KRW")
                .rate(rate)
                .date(date)
                .build());
    }

    // ✅ 오늘 기준 요약 응답
    public ExchangeSummaryDto getTodayExchangeSummary() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Exchange todayRate = exchangeRepository.findByBaseCodeAndTargetCodeAndDate("USD", "KRW", today)
                .orElseThrow(() -> new RuntimeException("오늘 환율 없음"));

        Exchange yesterdayRate = exchangeRepository.findByBaseCodeAndTargetCodeAndDate("USD", "KRW", yesterday)
                .orElse(null); // ❗ 어제 데이터 없을 수도 있음

        double diff = 0;
        double percent = 0;

        if (yesterdayRate != null) {
            diff = Math.round((todayRate.getRate() - yesterdayRate.getRate()) * 10.0) / 10.0;
            percent = Math.round((diff / yesterdayRate.getRate()) * 10000.0) / 100.0;
        }

        return new ExchangeSummaryDto(todayRate.getRate(), diff, percent);
    }

    // 오늘 데이터 없을 때 외부 API에서 받아와서 DB에 저장
    public void saveTodayExchangeIfAbsent() {
        LocalDate today = LocalDate.now();

        if (existsByDate(today)) return; // 이미 있으면 스킵

        ExchangeRateResponse response = fetchFromApi();
        if (response == null || response.getConversion_rates() == null) return;

        Double krwRate = response.getConversion_rates().get("KRW");
        if (krwRate != null) {
            saveRate(krwRate, today);
        }
    }
}