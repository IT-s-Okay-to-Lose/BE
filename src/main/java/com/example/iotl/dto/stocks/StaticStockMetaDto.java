package com.example.iotl.dto.stocks;

import com.example.iotl.entity.Stocks;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaticStockMetaDto {

    @Schema(description = "종목 코드", example = "005930")
    private final String code;

    @Schema(description = "종목 이름", example = "삼성전자")
    private final String name;

    @Schema(description = "시장 구분", example = "KOSPI")
    private final String marketType;

    @Schema(description = "종목 로고 이미지 URL", example = "https://logo.clearbit.com/samsung.com")
    private final String imageUrl;

    public static StaticStockMetaDto from(Stocks stocks) {
        return StaticStockMetaDto.builder()
                .code(stocks.getStockCode())
                .name(stocks.getStockName())
                .marketType(stocks.getMarketType())
                .imageUrl(stocks.getLogoUrl())
                .build();
    }
}