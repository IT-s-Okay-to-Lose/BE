package com.example.iotl.scheduler;

import com.example.iotl.dto.stocks.VolumeDataDto;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.handler.VolumeWebSocketHandler;
import com.example.iotl.service.stock.StockApiService;
import com.example.iotl.service.stock.StockService;
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

    private final StockApiService stockApiService;
    private final StockService stockService;
    private final VolumeWebSocketHandler volumeWebSocketHandler;
    private final ObjectMapper objectMapper;
    private final BaseResponseService baseResponseService;

    private final Map<String, VolumeDataDto> lastSentVolumeMap = new HashMap<>();

    public VolumeScheduler(
            StockApiService stockApiService,
            StockService stockService,
            VolumeWebSocketHandler volumeWebSocketHandler,
            BaseResponseService baseResponseService
    ) {
        this.stockApiService = stockApiService;
        this.stockService = stockService;
        this.volumeWebSocketHandler = volumeWebSocketHandler;
        this.baseResponseService = baseResponseService;

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Scheduled(fixedRate = 30000)
    public void sendVolumeData() {
        Map<String, VolumeWebSocketHandler.VolumeRequest> sessionMap = volumeWebSocketHandler.getSessionRequestMap();

        for (Map.Entry<String, VolumeWebSocketHandler.VolumeRequest> entry : sessionMap.entrySet()) {
            String sessionId = entry.getKey();
            VolumeWebSocketHandler.VolumeRequest request = entry.getValue();

            for (String code : request.getCodes()) {
                try {
                    Map<String, String> output = fetchOutput(code);
                    if (output == null) continue;

                    VolumeDataDto volumeData = VolumeDataDto.from(output);
                    sendVolumeToSession(sessionId, code, volumeData);
                    lastSentVolumeMap.put(code, volumeData);

                } catch (Exception e) {
                    log.error("❌ 거래량 전송 실패 for {} to {}", code, sessionId, e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> fetchOutput(String code) {
        Map<String, Object> result = stockApiService.getStockPrice(code);
        return (Map<String, String>) result.get("output");
    }

    private void sendVolumeToSession(String sessionId, String code, VolumeDataDto volumeData) throws Exception {
        Map<String, Object> resultMap = Map.of(
                "volume", List.of(volumeData.getTime(), volumeData.getVolume())
        );

        BaseResponse<Object> response = baseResponseService.getSuccessResponse(resultMap);
        String json = objectMapper.writeValueAsString(response);
        volumeWebSocketHandler.sendToSession(sessionId, json);
    }
}