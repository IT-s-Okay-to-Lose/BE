package com.example.iotl.handler;

import com.example.iotl.dto.stocks.VolumeDataDto;
import com.example.iotl.entity.StockDetail;
import com.example.iotl.service.StockService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class VolumeWebSocketHandler extends TextWebSocketHandler {

    private boolean marketOpen = true;

    public void setMarketOpen(boolean open) {
        this.marketOpen = open;
    }

    @Getter
    public static class VolumeRequest {
        private final List<String> codes;

        public VolumeRequest(List<String> codes) {
            this.codes = codes;
        }
    }

    private final Map<String, VolumeRequest> sessionRequestMap = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public Map<String, VolumeRequest> getSessionRequestMap() {
        return sessionRequestMap;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("📡 Volume WebSocket 연결됨: {}", session.getId());
        sessions.put(session.getId(), session);
    }

    private final StockService stockService;
    private final ObjectMapper objectMapper;

    public VolumeWebSocketHandler(StockService stockService) {
        this.stockService = stockService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload(); // 예: "005930,000660"
            List<String> codes = Arrays.asList(payload.split(","));
            sessionRequestMap.put(session.getId(), new VolumeRequest(codes));

            log.info("📨 Volume 구독 요청: {}", codes);

            // 즉시 응답 보내기 - 최근 거래량
            for (String code : codes) {
                StockDetail latest = stockService.findLatestStockByCode(code);
                if (latest != null) {
                    VolumeDataDto volumeData = new VolumeDataDto(latest);

                    Map<String, Object> result = new HashMap<>();
                    result.put("code", code);
                    result.put("volume", volumeData);

                    String json = objectMapper.writeValueAsString(result);
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (Exception e) {
            log.error("❌ Volume 요청 파싱 또는 전송 실패", e);
        }
    }

    public void sendToSession(String sessionId, String message) {
        if (!marketOpen) {
            //log.info("⏸️ 장외 시간 - 차트 메시지 전송 생략");
            return;
        }

        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("❌ 세션 {} 메시지 전송 실패: {}", sessionId, e.getMessage());
            }
        }
    }

    // 연결 종료 시 세션 아이디 없애기
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRequestMap.remove(session.getId());
        sessions.remove(session.getId());
    }

    // 세션 닫기
    public void closeAllSessions() {
        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) session.close();
            } catch (IOException e) {
                log.error("❌ 세션 닫기 실패: {}", e.getMessage());
            }
        }
        sessions.clear();
        sessionRequestMap.clear(); // chart에서는 필요
    }
}