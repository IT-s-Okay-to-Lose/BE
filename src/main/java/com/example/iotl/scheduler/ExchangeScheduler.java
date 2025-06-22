package com.example.iotl.scheduler;

import com.example.iotl.dto.exchange.ExchangeRateResponse;
import com.example.iotl.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ExchangeScheduler {

    private final ExchangeService exchangeService;

    // 매일 자정에 실행되도록 원래는 cron 사용
    // @Scheduled(cron = "0 0 0 * * *") // 매일 00시에 불러와서 하루 환율 저장해서 비교하기 위해서 넣음 시간은 정하면 될듯!
    // @Scheduled(fixedRate = 10000) // 테스트용으로 10초마다 실행
    public void saveDailyExchangeRate() {
        LocalDate today = LocalDate.now();

        // 이미 저장되어 있다면 중복 저장 방지
        if (exchangeService.existsByDate(today)) {
            return;
        }

        ExchangeRateResponse response = exchangeService.fetchFromApi();
        Double krwRate = response.getConversion_rates().get("KRW");

        if (krwRate != null) {
            exchangeService.saveRate(krwRate, today);
        }
    }
}