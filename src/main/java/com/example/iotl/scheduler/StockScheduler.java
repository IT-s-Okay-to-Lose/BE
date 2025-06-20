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


    // 변경 여부 판단 (하나라도 다르면 true)
    private boolean isChanged(DynamicStockDataDto newDto, DynamicStockDataDto oldDto) {
        if (oldDto == null) {
            log.info("🆕 [{}] 신규 데이터 감지 (기존 데이터 없음)", newDto.getCode());
            return true;
        }

        boolean priceChanged = newDto.getCurrentPrice().compareTo(oldDto.getCurrentPrice()) != 0;
        boolean rateChanged = newDto.getFluctuationRate().compareTo(oldDto.getFluctuationRate()) != 0;
        boolean volumeChanged = !Objects.equals(newDto.getAccumulatedVolume(), oldDto.getAccumulatedVolume());

        log.info("🔍 [{}] 비교 결과 → priceChanged={}, rateChanged={}, volumeChanged={}",
                newDto.getCode(), priceChanged, rateChanged, volumeChanged);

        log.info("📊 [{}] 이전: price={}, rate={}, volume={}",
                newDto.getCode(), oldDto.getCurrentPrice(), oldDto.getFluctuationRate(), oldDto.getAccumulatedVolume());
        log.info("📊 [{}] 현재: price={}, rate={}, volume={}",
                newDto.getCode(), newDto.getCurrentPrice(), newDto.getFluctuationRate(), newDto.getAccumulatedVolume());

        return priceChanged || rateChanged || volumeChanged;
    }

    @Scheduled(fixedRate = 5000)
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

        List<DynamicStockDataDto> updatedList = new ArrayList<>();

        for (String code : batch) {
            try {
                stockService.saveStockPrice(code); // DB 저장

                StockDetail latest = stockService.findLatestStockByCode(code);
                if (latest == null) continue;

                DynamicStockDataDto newDto = new DynamicStockDataDto(latest);
                DynamicStockDataDto prev = lastSentMap.get(code);

                if (isChanged(newDto, prev)) {
                    updatedList.add(newDto);
                    lastSentMap.put(code, newDto);
                    log.info("✅ [{}] 변경 감지됨 → currentPrice={}, fluctuationRate={}, volume={}",
                            code, newDto.getCurrentPrice(), newDto.getFluctuationRate(), newDto.getAccumulatedVolume());
                } else {
                    log.info("🔁 [{}] 변화 없음 → currentPrice={}, fluctuationRate={}, volume={}",
                            code, newDto.getCurrentPrice(), newDto.getFluctuationRate(), newDto.getAccumulatedVolume());
                }

            } catch (Exception e) {
                log.error("❌ [{}] 처리 실패: {}", code, e.getMessage());
            }
        }

        if (!updatedList.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(updatedList);
                stockWebSocketHandler.broadcast(json);
                log.info("📡 실시간 데이터 {}건 전송", updatedList.size());
            } catch (Exception e) {
                log.error("❌ WebSocket 전송 실패: {}", e.getMessage());
            }
        }

        currentIndex = (currentIndex + BATCH_SIZE) % totalStocks;
    }
}