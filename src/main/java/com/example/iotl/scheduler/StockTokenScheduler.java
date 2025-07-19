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

    @Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Seoul")
    @Scheduled(cron = "0 5 12 * * ?", zone = "Asia/Seoul")
    public void refreshAccessToken() {
        stockApiService.refreshAccessToken();
    }
}