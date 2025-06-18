package com.example.iotl.dto.dashboard;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserInvestmentSummaryDto {

    private Long totalCash;     // 원금
    private Long totalProfit;   // 총 수익
    private Double roi;         // 수익률 (%) = (수익 / 원금) * 100

    public UserInvestmentSummaryDto(Long totalCash, Long totalProfit) {
        this.totalCash = totalCash;
        this.totalProfit = totalProfit;
        this.roi = calculateRoi(totalCash, totalProfit);
    }

    private double calculateRoi(Long totalCash, Long totalProfit) {
        if (totalCash == null || totalCash == 0) {
            return 0.0;
        }
        return Math.round((totalProfit / (double) totalCash) * 10000.0) / 100.0; // 소수점 둘째자리까지
    }
}