package com.example.iotl.dto.order;

import com.example.iotl.entity.Order;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor           // ✅ Jackson 역직렬화에 필요
public class OrderRequestDto {
    private String stockCode;
    private Order.OrderType orderType; // BUY / SELL
    private BigDecimal price;
    private Integer quantity;          // int 대신 Integer 권장
}
