package com.example.iotl.scheduler;

import com.example.iotl.service.MarketIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketIndexScheduler {

    private final MarketIndexService marketIndexService;

    // 매일 아침 9시 5분마다 저장
    @Scheduled(cron = "0 5 9 * * ?") // 매일 09시 5분에 불러와서 하루 환율 저장해서 비교하기 위해서 넣음 시간은 정하면 될듯!
    public void fetchMarketIndices() {
        try {
            marketIndexService.saveMarketIndex("KOSPI");
            marketIndexService.saveMarketIndex("KOSDAQ");
            log.info("Market indices saved successfully.");
        } catch (Exception e) {
            log.error("Market indices save failed.", e);
        }
    }
}