package com.example.iotl.dto.realized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RealizedProfitSummaryDto {
    private Long totalIncome;
    private Long dividendIncome; // 배당 수익
    private Long saleIncome; // 실현 매매 수익(매도가 - 매수평단가) × 수량, SELL + COMPLETED
}
