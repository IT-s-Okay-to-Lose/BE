package com.example.iotl.scheduler;

import com.example.iotl.service.marketIndex.MarketIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketIndexScheduler {

    private final MarketIndexService marketIndexService;

    // 매일 아침 8시 저장
    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Seoul") // 매일 08:00
    public void fetchMarketIndices() {
        saveMarketIndexWithLog("KOSPI");
        saveMarketIndexWithLog("KOSDAQ");
    }

    private void saveMarketIndexWithLog(String marketType) {
        try {
            boolean saved = marketIndexService.saveIfAbsent(marketType);
            if (saved) {
                log.info("[✅] {} 지수 저장 완료", marketType);
            } else {
                log.info("[🔁] {} 지수는 이미 저장되어 있음", marketType);
            }
        } catch (Exception e) {
            log.error("[❌] {} 지수 저장 실패: {}", marketType, e.getMessage(), e);
        }
    }
}