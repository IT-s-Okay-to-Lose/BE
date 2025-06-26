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

   //@Scheduled(fixedRate = 5000)
    public void sendVolumeData() {
        Map<String, VolumeWebSocketHandler.VolumeRequest> sessionMap = volumeWebSocketHandler.getSessionRequestMap();

        for (Map.Entry<String, VolumeWebSocketHandler.VolumeRequest> entry : sessionMap.entrySet()) {
            String sessionId = entry.getKey();
            VolumeWebSocketHandler.VolumeRequest request = entry.getValue();

            for (String code : request.getCodes()) {
                try {
                    StockDetail latest = stockService.findLatestStockByCode(code);
                    if (latest == null) continue;

                    VolumeDataDto volumeData = new VolumeDataDto(latest);
                    VolumeDataDto prevVolume = lastSentVolumeMap.get(code);

                    if (prevVolume == null || !volumeData.getTime().equals(prevVolume.getTime())) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("volume", List.of(
                                volumeData.getTime().toString(),
                                volumeData.getVolume()
                        ));

                        String json = objectMapper.writeValueAsString(result);
                        volumeWebSocketHandler.sendToSession(sessionId, json);

                        log.info("📦 거래량 전송: {} - {} to {}", code, volumeData.getTime(), sessionId);
                        lastSentVolumeMap.put(code, volumeData);
                    } else {
                        log.info("⏸ 동일 거래량 time({}) for {}", volumeData.getTime(), code);
                    }

                } catch (Exception e) {
                    log.error("❌ 거래량 전송 실패 for {} to {}", code, sessionId, e);
                }
            }
        }
    }
}