package com.example.iotl.scheduler;

import com.example.iotl.dto.stocks.CandleDataDto;
import com.example.iotl.dto.stocks.MarketStockPriceInfoDto;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.handler.ChartWebSocketHandler;
import com.example.iotl.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChartScheduler {

    private final StockService stockService;
    private final ChartWebSocketHandler chartWebSocketHandler;
    private final ObjectMapper objectMapper;

    public ChartScheduler(StockService stockService, ChartWebSocketHandler chartWebSocketHandler) {
        this.stockService = stockService;
        this.chartWebSocketHandler = chartWebSocketHandler;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    //@Scheduled(fixedRate = 5000)
    public void sendChartDataToSubscribers() {
        Map<String, ChartWebSocketHandler.ChartRequest> sessionMap = chartWebSocketHandler.getSessionRequestMap();

        for (Map.Entry<String, ChartWebSocketHandler.ChartRequest> entry : sessionMap.entrySet()) {
            String sessionId = entry.getKey();
            ChartWebSocketHandler.ChartRequest request = entry.getValue();

            for (String code : request.codes()) {
                try {
                    StockDetail latest = stockService.findLatestStockByCode(code);
                    if (latest == null) continue;

                    MarketStockPriceInfoDto priceInfo = new MarketStockPriceInfoDto(latest);
                    List<StockDetail> allDetails = stockService.findStocksByCode(code);
                    List<CandleDataDto> candles = filterByInterval(allDetails, request.interval());

                    Map<String, Object> result = new HashMap<>();
                    result.put("code", code);
                    result.put("interval", request.interval());
                    result.put("price", priceInfo);
                    result.put("candles", candles);

                    String json = objectMapper.writeValueAsString(result);
                    chartWebSocketHandler.sendToSession(sessionId, json);

                    log.info("📈 [{}] {} 데이터 전송 완료 to session {}", code, request.interval(), sessionId);

                } catch (Exception e) {
                    log.error("❌ [{}] 전송 실패 to session {}", code, sessionId, e);
                }
            }
        }
    }

    private List<CandleDataDto> filterByInterval(List<StockDetail> details, String interval) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;

        switch (interval) {
            case "1m" -> from = now.minusMinutes(1);
            case "5m" -> from = now.minusMinutes(5);
            case "1h" -> from = now.minusHours(1);
            case "1d" -> from = now.minusDays(1);
            default -> from = now.minusSeconds(30); // "live" 또는 기타
        }

        return details.stream()
                .filter(d -> !d.getCreatedAt().isBefore(from))
                .map(CandleDataDto::new)
                .collect(Collectors.toList());
    }
}