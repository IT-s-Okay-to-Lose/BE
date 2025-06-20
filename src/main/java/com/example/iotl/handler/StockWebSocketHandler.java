package com.example.iotl.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockWebSocketHandler extends TextWebSocketHandler {

    // 접속 중인 WebSocket 세션 목록
    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    // 클라이언트가 접속했을 때
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("📶 클라이언트 접속: {}", session.getId());
    }

    // 클라이언트가 연결 종료했을 때
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("❎ 클라이언트 연결 종료: {}", session.getId());
    }

    // 외부에서 메시지를 보내기 위한 메서드 (예: StockScheduler에서 호출)
    public void broadcast(String message) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("❌ WebSocket 메시지 전송 중 오류 발생: {}", e.getMessage());
            }
        }
    }
}