package com.example.iotl.dto;

import com.example.iotl.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderResponseDto {
    private Long orderId;
    private String stockCode;
    private String orderType; // "BUY" or "SELL"
    private BigDecimal orderPrice;
    private int quantity;
    private String status;
    private LocalDateTime createdAt;

    public static OrderResponseDto from(Order order) {
        return OrderResponseDto.builder()
            .orderId(order.getId())
            .stockCode(order.getStock().getStockCode())
            .orderType(String.valueOf(order.getOrderType()))
            .orderPrice(order.getPrice())
            .quantity(order.getQuantity())
            .status(String.valueOf(order.getStatus()))
            .createdAt(order.getCreatedAt())
            .build();
    }
}
