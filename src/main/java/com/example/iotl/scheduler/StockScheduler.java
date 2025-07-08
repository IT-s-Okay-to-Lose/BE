package com.example.iotl.scheduler;

import com.example.iotl.dto.stocks.DynamicStockDataDto;
import com.example.iotl.handler.StockWebSocketHandler;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.service.stock.StockApiService;
import com.example.iotl.service.stock.StockService;
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

    private final StockApiService stockApiService;
    private final StockService stockService;
    private final StockInfoRepository stockInfoRepository;
    private final StockWebSocketHandler stockWebSocketHandler;
    private final ObjectMapper objectMapper;

    private int currentIndex = 0;
    private static final int BATCH_SIZE = 5;

    // 종목 코드 기준으로 마지막 전송한 데이터 보관
    private final Map<String, DynamicStockDataDto> lastSentMap = new HashMap<>();

    public StockScheduler(StockApiService stockApiService, StockService stockService,
                          StockInfoRepository stockInfoRepository,
                          StockWebSocketHandler stockWebSocketHandler) {
        this.stockApiService = stockApiService;
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

        if (totalStocks == 0) return;

        List<String> batch = getCurrentBatch(stockCodes, totalStocks);
        List<DynamicStockDataDto> updatedList = new ArrayList<>();

        for (String code : batch) {
            try {
                Map<String, Object> result = stockApiService.getStockPrice(code);
                Map<String, String> output = castOutput(result.get("output"));
                if (output == null) continue;

                updatedList.add(DynamicStockDataDto.from(output, code));
            } catch (Exception e) {
                log.error("❌ [{}] 실시간 데이터 조회 실패: {}", code, e.getMessage());
            }
        }

        sendToClients(updatedList);
        currentIndex = (currentIndex + BATCH_SIZE) % totalStocks;
    }

    private List<String> getCurrentBatch(List<String> stockCodes, int totalStocks) {
        List<String> batch = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            int idx = (currentIndex + i) % totalStocks;
            batch.add(stockCodes.get(idx));
        }
        return batch;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> castOutput(Object output) {
        if (output instanceof Map) {
            return (Map<String, String>) output;
        }
        return null;
    }

    private void sendToClients(List<DynamicStockDataDto> dataList) {
        if (dataList.isEmpty()) return;

        try {
            String json = objectMapper.writeValueAsString(dataList);
            stockWebSocketHandler.broadcast(json);
        } catch (Exception e) {
            log.error("❌ WebSocket 전송 실패: {}", e.getMessage());
        }
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