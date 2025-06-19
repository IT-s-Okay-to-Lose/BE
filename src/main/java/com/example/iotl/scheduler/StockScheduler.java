package com.example.iotl.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import com.example.iotl.dto.stocks.StockPriceDto;
import com.example.iotl.handler.StockWebSocketHandler;
import com.example.iotl.repository.StockInfoRepository;
import com.example.iotl.service.StockService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class StockScheduler {

    private final StockService stockService;
    private final StockInfoRepository stockInfoRepository;
    private final StockWebSocketHandler stockWebSocketHandler;
    private final ObjectMapper objectMapper;

    private int currentIndex = 0;
    private static final int BATCH_SIZE = 5;

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

//    @Scheduled(fixedRate = 5000)
    public void fetchStockDataBatch() {
        List<String> stockCodes = stockInfoRepository.findAllStockCodes();
        int totalStocks = stockCodes.size();

        if (totalStocks == 0) {
            log.warn("⚠️ 종목 코드가 존재하지 않습니다.");
            return;
        }

        List<String> batch = new ArrayList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            int idx = (currentIndex + i) % totalStocks;
            batch.add(stockCodes.get(idx));
        }

        List<StockPriceDto> collected = new ArrayList<>();

        for (String code : batch) {
            try {
                StockPriceDto dto = stockService.saveStockPrice(code); // StockDetail 기반 DTO
                collected.add(dto);
                log.info("✅ {} 저장 및 수신 성공", code);
            } catch (Exception e) {
                log.error("❌ {} 저장 실패: {}", code, e.getMessage());
            }
        }

        try {
            String json = objectMapper.writeValueAsString(collected);
            stockWebSocketHandler.broadcast(json);
        } catch (Exception e) {
            log.error("❌ WebSocket 전송 실패: {}", e.getMessage());
        }

        currentIndex = (currentIndex + BATCH_SIZE) % totalStocks;
    }
}