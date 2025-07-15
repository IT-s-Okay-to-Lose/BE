package com.example.iotl.scheduler;

import com.example.iotl.service.stock.StockApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockTokenScheduler {

    private final StockApiService stockApiService;

    @Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Seoul")  // 매일 00:05
    public void refreshTokenMidnight() {
        try {
            stockApiService.refreshAccessToken();
            log.info("🔄 [00:05] 토큰 갱신 완료");
        } catch (Exception e) {
            log.error("❌ [00:05] 토큰 갱신 실패: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 5 12 * * ?", zone = "Asia/Seoul")  // 매일 12:05
    public void refreshTokenNoon() {
        try {
            stockApiService.refreshAccessToken();
            log.info("🔄 [12:05] 토큰 갱신 완료");
        } catch (Exception e) {
            log.error("❌ [12:05] 토큰 갱신 실패: {}", e.getMessage());
        }
    }
}