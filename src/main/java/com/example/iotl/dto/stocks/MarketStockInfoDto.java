package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 디테일 페이지 종목 기본 정보 DTO
@Getter
@Builder
@AllArgsConstructor
public class MarketStockInfoDto {

    @Schema(description = "종목 로고 이미지 URL", example = "https://logo.clearbit.com/samsung.com")
    private final String imageUrl;

    @Schema(description = "종목 이름", example = "삼성전자")
    private final String name;

    @Schema(description = "종목 코드", example = "005930")
    private final String code;

    public static MarketStockInfoDto from(StockDetail stockDetail) {
        return MarketStockInfoDto.builder()
            .imageUrl(stockDetail.getStocks().getLogoUrl())
            .name(stockDetail.getStocks().getStockName())
            .code(stockDetail.getStocks().getStockCode())
            .build();
    }
}