package com.example.iotl.handler;

import com.example.iotl.global.response.BaseResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private boolean marketOpen = true;

    public void setMarketOpen(boolean open) {
        this.marketOpen = open;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    /**
     * 실시간 주식 정보를 모든 클라이언트에게 전송 (BaseResponse 형식으로 감쌈)
     * @param rawData List, Map, DTO 등 직렬화 가능한 객체
     */
    public void broadcast(Object rawData) {
        if (!marketOpen) return;

        // BaseResponse 객체 생성
        BaseResponse<Object> response = BaseResponse.<Object>builder()
                .isSuccess(true)
                .code(200)
                .message("실시간 주식 정보입니다.")
                .data(rawData) // ⚠️ 객체 그대로 넣기 (String으로 넣지 마시오!)
                .build();

        try {
            String json = objectMapper.writeValueAsString(response); // 직렬화
            TextMessage message = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (IOException e) {
            log.error("❌ WebSocket 메시지 전송 실패", e);
        }
    }

    public void closeAllSessions() {
        for (WebSocketSession session : sessions) {
            try {
                if (session.isOpen()) session.close();
            } catch (IOException e) {
                log.error("❌ 세션 닫기 실패", e);
            }
        }
        sessions.clear();
    }
}