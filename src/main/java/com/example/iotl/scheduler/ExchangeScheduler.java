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

    @Scheduled(cron = "0 50 8 * * ?")// 매일 08시 50분에 불러와서 하루 환율 저장해서 비교하기 위해서 넣음 시간은 정하면 될듯!
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