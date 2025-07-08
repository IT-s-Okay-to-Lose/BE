package com.example.iotl.dto;

import com.example.iotl.entity.Order;
import com.example.iotl.entity.Order.OrderStatus;
import com.example.iotl.entity.Order.OrderType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryDto {
    private Long orderId;
    private OrderType orderType;
    private String stockCode;
    private int quantity;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public static OrderHistoryDto from(Order order) {
        return OrderHistoryDto.builder()
            .orderId(order.getId())
            .orderType(order.getOrderType())
            .stockCode(order.getStock().getStockCode())
            .quantity(order.getQuantity())
            .price(order.getPrice())
            .status(order.getStatus())
            .createdAt(order.getCreatedAt())
            .build();
    }
}
