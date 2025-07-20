package com.example.iotl.scheduler;

import com.example.iotl.dto.stocks.CandleDataDto;
import com.example.iotl.dto.stocks.MarketStockPriceInfoDto;
import com.example.iotl.handler.ChartWebSocketHandler;
import com.example.iotl.service.stock.StockApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class ChartScheduler {

    private final StockApiService stockApiService;
    private final ChartWebSocketHandler chartWebSocketHandler;
    private final ObjectMapper objectMapper;
    private final Map<String, CandleDataDto> lastSentCandleMap = new HashMap<>();

    public ChartScheduler(StockApiService stockApiService, ChartWebSocketHandler chartWebSocketHandler) {
        this.stockApiService = stockApiService;
        this.chartWebSocketHandler = chartWebSocketHandler;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Scheduled(fixedRate = 30000)
    public void sendChartDataToSubscribers() {
        chartWebSocketHandler.getSessionRequestMap().forEach((sessionId, request) -> {
            for (String code : request.codes()) {
                try {
                    Map<String, Object> result = stockApiService.getStockPrice(code);
                    Map<String, String> output = (Map<String, String>) result.get("output");
                    if (output == null) continue;

                    CandleDataDto newCandle = CandleDataDto.from(output);
                    MarketStockPriceInfoDto marketInfo = MarketStockPriceInfoDto.from(output);

                    String key = code + "_" + request.interval();
                    CandleDataDto prevCandle = lastSentCandleMap.get(key);

                    if (prevCandle == null || !newCandle.getTime().equals(prevCandle.getTime())) {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("candle", List.of(
                                newCandle.getTime(),
                                newCandle.getOpen(),
                                newCandle.getHigh(),
                                newCandle.getLow(),
                                newCandle.getClose()
                        ));
                        resultMap.put("marketInfo", marketInfo);

                        // ✅ json 문자열 대신 raw Map 전달
                        chartWebSocketHandler.sendToSession(sessionId, resultMap);
                        lastSentCandleMap.put(key, newCandle);
                    }

                } catch (Exception e) {
                    log.error("❌ [{}] 전송 실패 to session {}", code, sessionId, e);
                }
            }
        });
    }
}