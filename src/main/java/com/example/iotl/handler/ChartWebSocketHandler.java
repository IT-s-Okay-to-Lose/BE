package com.example.iotl.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class ChartWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, ChartRequest> sessionRequestMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        sessionRequestMap.remove(session.getId());
    }

    public void sendToSession(String sessionId, String message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("❌ 세션 {} 메시지 전송 실패: {}", sessionId, e.getMessage());
            }
        }
    }

    public Map<String, ChartRequest> getSessionRequestMap() {
        return sessionRequestMap;
    }
    // ✅ 내부 DTO 형태 정의
    public record ChartRequest(List<String> codes, String interval) {}
}