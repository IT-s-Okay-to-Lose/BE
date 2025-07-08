package com.example.iotl.scheduler;

import com.example.iotl.dto.stocks.CandleDataDto;
import com.example.iotl.dto.stocks.MarketStockPriceInfoDto;
import com.example.iotl.handler.ChartWebSocketHandler;
import com.example.iotl.service.stock.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class ChartScheduler {
    private final StockService stockService;
    private final ChartWebSocketHandler chartWebSocketHandler;
    private final ObjectMapper objectMapper;

    // ✅ code+interval 기준 마지막 전송된 candle 보관
    private final Map<String, CandleDataDto> lastSentCandleMap = new HashMap<>();

    public ChartScheduler(StockService stockService, ChartWebSocketHandler chartWebSocketHandler) {
        this.stockService = stockService;
        this.chartWebSocketHandler = chartWebSocketHandler;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Scheduled(fixedRate = 30000)
    public void sendChartDataToSubscribers() {
        Map<String, ChartWebSocketHandler.ChartRequest> sessionMap = chartWebSocketHandler.getSessionRequestMap();

        for (Map.Entry<String, ChartWebSocketHandler.ChartRequest> entry : sessionMap.entrySet()) {
            String sessionId = entry.getKey();
            ChartWebSocketHandler.ChartRequest request = entry.getValue();

            for (String code : request.codes()) {
                try {
                    Map<String, Object> result = stockService.getStockPrice(code);
                    Map<String, String> output = (Map<String, String>) result.get("output");

                    if (output == null) continue;

                    // 한국투자 API 결과 기반으로 CandleDataDto 생성
                    CandleDataDto lastCandle = CandleDataDto.builder()
                            .time(String.valueOf(LocalDateTime.now())) // 실시간이므로 현재 시간 사용
                            .open(new BigDecimal(output.get("stck_oprc")))
                            .high(new BigDecimal(output.get("stck_hgpr")))
                            .low(new BigDecimal(output.get("stck_lwpr")))
                            .close(new BigDecimal(output.get("stck_prpr")))
                            .build();

                    // MarketStockPriceInfoDto 생성
                    MarketStockPriceInfoDto marketInfo = MarketStockPriceInfoDto.builder()
                            .currentPrice(new BigDecimal(output.get("stck_prpr")))
                            .priceChange(new BigDecimal(output.get("prdy_vrss")))
                            .fluctuationRate(new BigDecimal(output.get("prdy_ctrt")))
                            .build();

                    String key = code + "_" + request.interval();
                    CandleDataDto prevCandle = lastSentCandleMap.get(key);

                    if (prevCandle == null || !lastCandle.getTime().equals(prevCandle.getTime())) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("candle", List.of(
                                lastCandle.getTime().toString(),
                                lastCandle.getOpen(),
                                lastCandle.getHigh(),
                                lastCandle.getLow(),
                                lastCandle.getClose()
                        ));
                        resultMap.put("marketInfo", marketInfo);
                        String json = objectMapper.writeValueAsString(resultMap);
                        chartWebSocketHandler.sendToSession(sessionId, json);
                        lastSentCandleMap.put(key, lastCandle);
                    }
                } catch (Exception e) {
                    log.error("❌ [{}] 전송 실패 to session {}", code, sessionId, e);
                }
            }
        }
    }
}