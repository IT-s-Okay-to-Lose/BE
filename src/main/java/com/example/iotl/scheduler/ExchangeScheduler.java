package com.example.iotl.scheduler;

import com.example.iotl.service.exchange.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeScheduler {

    private final ExchangeService exchangeService;

    // 매일 아침 8시
    @Scheduled(cron = "0 0 8 * * ?" , zone = "Asia/Seoul")
    public void saveDailyExchangeRate() {
        LocalDate today = LocalDate.now();
        log.info("🕘 [Scheduler] 환율 저장 시도: {}", today);

        boolean saved = exchangeService.saveTodayExchangeIfAbsent();

        if (saved) {
            log.info("💾 오늘 환율 저장 완료");
        } else {
            log.warn("⚠️ 오늘 환율 저장 실패 또는 이미 저장됨");
        }
    }
}