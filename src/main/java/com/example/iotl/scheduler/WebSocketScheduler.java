package com.example.iotl.scheduler;

import com.example.iotl.handler.ChartWebSocketHandler;
import com.example.iotl.handler.StockWebSocketHandler;
import com.example.iotl.handler.VolumeWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class WebSocketScheduler {
    private final StockWebSocketHandler stockWebSocketHandler;
    private final ChartWebSocketHandler chartWebSocketHandler;
    private final VolumeWebSocketHandler volumeWebSocketHandler;


    @Scheduled(cron = "0 0 9 * * MON-FRI") // 장 시작
    public void openMarket() {
        stockWebSocketHandler.setMarketOpen(true);
        chartWebSocketHandler.setMarketOpen(true);
        volumeWebSocketHandler.setMarketOpen(true);
        log.info("📈 장 시작 - WebSocket 열림 (모든 핸들러 marketOpen = true)");
    }


    @Scheduled(cron = "0 30 15 * * MON-FRI") // 장 마감
    public void closeMarket() {
        stockWebSocketHandler.setMarketOpen(false);
        chartWebSocketHandler.setMarketOpen(false);
        volumeWebSocketHandler.setMarketOpen(false);

        stockWebSocketHandler.closeAllSessions();
        chartWebSocketHandler.closeAllSessions();
        volumeWebSocketHandler.closeAllSessions();
        log.info("📉 장 마감 - WebSocket 세션 모두 닫힘");
    }
}
