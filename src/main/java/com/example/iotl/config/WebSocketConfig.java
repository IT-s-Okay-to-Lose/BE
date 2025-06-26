package com.example.iotl.config;

import com.example.iotl.handler.ChartWebSocketHandler;
import com.example.iotl.handler.StockWebSocketHandler;
import com.example.iotl.handler.VolumeWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final StockWebSocketHandler stockWebSocketHandler;
    private final ChartWebSocketHandler chartWebSocketHandler;
    private final VolumeWebSocketHandler volumeWebSocketHandler;

    public WebSocketConfig(StockWebSocketHandler stockWebSocketHandler,
                           ChartWebSocketHandler chartWebSocketHandler,
                           VolumeWebSocketHandler volumeWebSocketHandler) { // ✅ 추가
        this.stockWebSocketHandler = stockWebSocketHandler;
        this.chartWebSocketHandler = chartWebSocketHandler;
        this.volumeWebSocketHandler = volumeWebSocketHandler; // ✅ 추가
    }


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 메인 페이지용 WebSocket
        registry.addHandler(stockWebSocketHandler, "/ws/stock")
                .setAllowedOrigins("*");

        // 차트 페이지용 WebSocket
        registry.addHandler(chartWebSocketHandler, "/ws/chart")
                .setAllowedOrigins("*");

        registry.addHandler(volumeWebSocketHandler, "/ws/volume")
                .setAllowedOrigins("*");
    }
}