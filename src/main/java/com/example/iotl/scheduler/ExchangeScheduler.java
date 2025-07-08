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

    // 매일 아침 8시마다 저장
    @Scheduled(cron = "0 0 8 * * ?")
    public void saveDailyExchangeRate() {
        LocalDate today = LocalDate.now();
        System.out.println("🕘 [Scheduler] 환율 저장 시도: " + today);

        if (exchangeService.existsByDate(today)) {
            System.out.println("✅ 이미 저장되어 있음. 스킵");
            return;
        }

        ExchangeRateResponse response = exchangeService.fetchFromApi();
        Double krwRate = response.getConversion_rates().get("KRW");

        if (krwRate != null) {
            exchangeService.saveRate(krwRate, today);
            System.out.println("💾 KRW 환율 저장 완료: " + krwRate);
        } else {
            System.out.println("❌ KRW 환율 없음!");
        }
    }
}