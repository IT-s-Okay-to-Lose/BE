package com.example.iotl.scheduler;

import com.example.iotl.dto.stocks.VolumeDataDto;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.handler.VolumeWebSocketHandler;
import com.example.iotl.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class VolumeScheduler {

    private final StockService stockService;
    private final VolumeWebSocketHandler volumeWebSocketHandler;
    private final ObjectMapper objectMapper;

    private final Map<String, VolumeDataDto> lastSentVolumeMap = new HashMap<>();

    public VolumeScheduler(StockService stockService, VolumeWebSocketHandler volumeWebSocketHandler) {
        this.stockService = stockService;
        this.volumeWebSocketHandler = volumeWebSocketHandler;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Scheduled(fixedRate = 30000)
    public void sendVolumeData() {
        Map<String, VolumeWebSocketHandler.VolumeRequest> sessionMap = volumeWebSocketHandler.getSessionRequestMap();

        for (Map.Entry<String, VolumeWebSocketHandler.VolumeRequest> entry : sessionMap.entrySet()) {
            String sessionId = entry.getKey();
            VolumeWebSocketHandler.VolumeRequest request = entry.getValue();

            for (String code : request.getCodes()) {
                try {
                    Map<String, Object> result = stockService.getStockPrice(code);
                    Map<String, String> output = (Map<String, String>) result.get("output");

                    if (output == null) continue;

                    // StockDetail 객체 직접 생성
                    StockDetail mockStock = StockDetail.builder()
                            .stockCode(code)
                            .volume(Long.parseLong(output.get("acml_vol")))
                            .createdAt(LocalDateTime.now())
                            .build();

                    VolumeDataDto volumeData = new VolumeDataDto(mockStock);

                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("volume", List.of(
                            volumeData.getTime(),
                            volumeData.getVolume()
                    ));

                    String json = objectMapper.writeValueAsString(resultMap);
                    volumeWebSocketHandler.sendToSession(sessionId, json);
                    lastSentVolumeMap.put(code, volumeData);
                } catch (Exception e) {
                    log.error("❌ 거래량 전송 실패 for {} to {}", code, sessionId, e);
                }
            }
        }
    }
}