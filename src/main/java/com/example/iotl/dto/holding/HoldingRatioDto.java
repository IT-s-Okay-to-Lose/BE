package com.example.iotl.dto.holding;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// 보유 종목 파이 차트 데이터 프론트 요청 형식
@Setter
@Getter
@AllArgsConstructor
public class HoldingRatioDto {
    @Schema(description = "종목명", example = "삼성전자")
    private String stockName;

    @Schema(description = "비율(%)", example = "30.5")
    private double percent;

    @Schema(description = "차트 색상 hex 코드", example = "#36A2EB")
    private String color;
}
