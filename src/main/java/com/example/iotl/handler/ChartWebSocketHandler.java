package com.example.iotl.handler;

import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
@Component
@RequiredArgsConstructor
public class ChartWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ChartRequest> sessionRequestMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BaseResponseService baseResponseService;

    private boolean marketOpen = true;

    public void setMarketOpen(boolean open) {
        this.marketOpen = open;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        sessionRequestMap.put(session.getId(), new ChartRequest(List.of(), "live"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            ChartRequest request = objectMapper.readValue(message.getPayload(), ChartRequest.class);
            sessionRequestMap.put(session.getId(), request);
        } catch (Exception e) {
            log.warn("❌ Chart 요청 파싱 실패: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        sessionRequestMap.remove(session.getId());
    }

    public void sendToSession(String sessionId, Map<String, Object> data) {
        if (!marketOpen) return;

        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                BaseResponse<Map<String, Object>> response = baseResponseService.getSuccessResponse(data);
                String json = objectMapper.writeValueAsString(response);
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.error("❌ 세션 {} 메시지 전송 실패: {}", sessionId, e.getMessage());
            }
        }
    }

    public Map<String, ChartRequest> getSessionRequestMap() {
        return sessionRequestMap;
    }

    public record ChartRequest(List<String> codes, String interval) {}

    public void closeAllSessions() {
        for (WebSocketSession session : sessions.values()) {
            try {
                if (session.isOpen()) session.close();
            } catch (IOException e) {
                log.error("❌ 세션 닫기 실패: {}", e.getMessage());
            }
        }
        sessions.clear();
        sessionRequestMap.clear();
    }
}