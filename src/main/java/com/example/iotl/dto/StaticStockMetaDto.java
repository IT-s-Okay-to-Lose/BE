package com.example.iotl.dto;

import com.example.iotl.entity.Stocks;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StaticStockMetaDto {

    @Schema(description = "종목 코드", example = "005930")
    private String code;

    @Schema(description = "종목 이름", example = "삼성전자")
    private String name;

    @Schema(description = "시장 구분", example = "KOSPI")
    private String marketType;

    @Schema(description = "종목 로고 이미지 URL", example = "https://logo.clearbit.com/samsung.com")
    private String imageUrl;

    public StaticStockMetaDto(Stocks stocks) {
        this.code = stocks.getStockCode();
        this.name = stocks.getStockName();
        this.marketType = stocks.getMarketType();
        this.imageUrl = stocks.getLogoUrl();
    }
}