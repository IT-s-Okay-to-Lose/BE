package com.example.iotl.dto;

import com.example.iotl.entity.Order.OrderType;
import com.example.iotl.entity.Trade;
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
public class TradeDto {

    private Long tradeId;
    private Long orderId;
    private String stockCode;
    private OrderType orderType;
    private BigDecimal executedPrice;
    private Integer executedQuantity;
    private LocalDateTime executedAt;

    public static TradeDto from(Trade trade) {
        return TradeDto.builder()
            .tradeId(trade.getId())
            .orderId(trade.getOrder().getId())
            .stockCode(trade.getOrder().getStock().getStockCode())
            .orderType(trade.getOrder().getOrderType())
            .executedPrice(trade.getExecutedPrice())
            .executedQuantity(trade.getExecutedQuantity())
            .executedAt(trade.getExecutedAt())
            .build();
    }
}
