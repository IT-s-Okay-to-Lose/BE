package com.example.iotl.dto.holding;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class MyStockSummaryDto {
    private BigDecimal averagePrice;   // 1주 평균 매입가 → 538,000원
    private int quantity;              // 보유 수량 → 100주
    private BigDecimal expectedFee;    // 수수료 → 164원 예상
    private BigDecimal totalProfit;    // 총 수익 → -19,243원
}
