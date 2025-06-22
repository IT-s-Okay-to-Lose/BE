package com.example.iotl.config;

import com.example.iotl.handler.ChartWebSocketHandler;
import com.example.iotl.handler.StockWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final StockWebSocketHandler stockWebSocketHandler;
    private final ChartWebSocketHandler chartWebSocketHandler;

    public WebSocketConfig(StockWebSocketHandler stockWebSocketHandler,
                           ChartWebSocketHandler chartWebSocketHandler) {
        this.stockWebSocketHandler = stockWebSocketHandler;
        this.chartWebSocketHandler = chartWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 메인 페이지용 WebSocket
        registry.addHandler(stockWebSocketHandler, "/ws/stock")
                .setAllowedOrigins("*");

        // 차트 페이지용 WebSocket
        registry.addHandler(chartWebSocketHandler, "/ws/chart")
                .setAllowedOrigins("*");
    }
}