package com.example.iotl.scheduler;

import com.example.iotl.dto.stocks.DynamicStockDataDto;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.handler.StockWebSocketHandler;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
@Slf4j
public class StockScheduler {

    private final StockService stockService;
    private final StockInfoRepository stockInfoRepository;
    private final StockWebSocketHandler stockWebSocketHandler;
    private final ObjectMapper objectMapper;

    private int currentIndex = 0;
    private static final int BATCH_SIZE = 5;

    // 종목 코드 기준으로 마지막 전송한 데이터 보관
    private final Map<String, DynamicStockDataDto> lastSentMap = new HashMap<>();

    public StockScheduler(StockService stockService,
                          StockInfoRepository stockInfoRepository,
                          StockWebSocketHandler stockWebSocketHandler) {
        this.stockService = stockService;
        this.stockInfoRepository = stockInfoRepository;
        this.stockWebSocketHandler = stockWebSocketHandler;

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Scheduled(fixedRate = 5000)
    public void fetchStockDataBatch() {
        List<String> stockCodes = stockInfoRepository.findAllStockCodes();
        int totalStocks = stockCodes.size();

        if (totalStocks == 0) {
            //log.warn("⚠️ 종목 코드가 존재하지 않습니다.");
            return;
        }

        List<String> batch = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            int idx = (currentIndex + i) % totalStocks;
            batch.add(stockCodes.get(idx));
        }
        List<DynamicStockDataDto> updatedList = new ArrayList<>();

        for (String code : batch) {
            try {
                Map<String, Object> result = stockService.getStockPrice(code);
                Map<String, String> output = (Map<String, String>) result.get("output");

                if (output == null) continue;

                DynamicStockDataDto dto = DynamicStockDataDto.builder()
                        .code(code)
                        .currentPrice(new BigDecimal(output.get("stck_prpr")))
                        .fluctuationRate(new BigDecimal(output.get("prdy_ctrt")))
                        .accumulatedVolume(Long.parseLong(output.get("acml_vol")))
                        .build();

                updatedList.add(dto);
            } catch (Exception e) {
//                log.error("❌ 실시간 주식 데이터 조회 실패: {}", e.getMessage());
            }
        }
        if (!updatedList.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(updatedList);
                stockWebSocketHandler.broadcast(json);
//                log.info("📡 실시간 데이터 {}건 전송", updatedList.size());
            } catch (Exception e) {
//                log.error("❌ WebSocket 전송 실패: {}", e.getMessage());
            }
        }
        currentIndex = (currentIndex + BATCH_SIZE) % totalStocks;
    }

    @Scheduled(cron = "0 31 15 * * MON-FRI") // 매주 월~금 15:31
    public void saveStockPriceAtMarketClose() {
        List<String> stockCodes = stockInfoRepository.findAllStockCodes();

        for (String code : stockCodes) {
            try {
                stockService.saveStockPrice(code); // 이 시점에만 DB 저장
                log.info("✅ [{}] 종가 저장 완료", code);
            } catch (Exception e) {
                log.error("❌ [{}] 종가 저장 실패: {}", code, e.getMessage());
            }
        }
    }
}