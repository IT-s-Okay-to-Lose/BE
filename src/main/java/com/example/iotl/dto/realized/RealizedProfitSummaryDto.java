package com.example.iotl.dto.realized;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "월별 실현 수익 요약 정보 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RealizedProfitSummaryDto {
    @Schema(description = "총 실현 수익 (배당 수익 + 실현 매매 수익)", example = "50000")
    private Long totalIncome;

    @Schema(description = "배당 수익", example = "25000")
    private Long dividendIncome;

    @Schema(description = "실현 매매 수익 ((체결가 - 매수 평균가) × 수량)", example = "25000")
    private Long saleIncome;
}
