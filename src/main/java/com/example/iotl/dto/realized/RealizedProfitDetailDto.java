package com.example.iotl.dto.realized;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Schema(description = "월별 실현 수익 상세 항목 DTO (종목별 수익 정보)")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RealizedProfitDetailDto {
    @Schema(description = "주식 이름", example = "삼성전자")
    private String stockName;
    @Schema(description = "수익 종류(판매수익 or 배당금)", example = "판매수익")
    private String type; // "판매수익" 또는 "배당금"
    @Schema(description = "수익 금액(단위: 원)", example = "25000")
    private Long amount;
}