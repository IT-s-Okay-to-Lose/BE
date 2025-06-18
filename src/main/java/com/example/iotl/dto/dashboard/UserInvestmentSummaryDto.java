package com.example.iotl.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "유저 투자 정보")
@Getter
@NoArgsConstructor
public class UserInvestmentSummaryDto {

    @Schema(description = "원금", example = "10000000000")
    private Long totalCash;     // 원금
    @Schema(description = "총 수익", example = "2000000")
    private Long totalProfit;   // 총 수익
    @Schema(description = "수익률 (%) = (수익 / 원금) * 100", example = "0.02")
    private Double roi;         // 수익률 (%) = (수익 / 원금) * 100

    public UserInvestmentSummaryDto(Long totalCash, Long totalProfit, Double roi) {
        this.totalCash = totalCash;
        this.totalProfit = totalProfit;
        this.roi = roi;
    }

    private double calculateRoi(Long totalCash, Long totalProfit) {
        if (totalCash == null || totalCash == 0) {
            return 0.0;
        }
        return Math.round((totalProfit / (double) totalCash) * 10000.0) / 100.0; // 소수점 둘째자리까지
    }
}