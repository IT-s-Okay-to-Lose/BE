package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@EqualsAndHashCode(of = {"id", "code"})
public class MarketStockDto {

    @Schema(description = "주식 상세 정보 ID", example = "1001")
    private final Long id;

    @Schema(description = "종목 로고 이미지 URL", example = "https://logo.clearbit.com/samsung.com")
    private final String imageUrl;

    @Schema(description = "종목 이름", example = "삼성전자")
    private final String name;

    @Schema(description = "종목 코드", example = "005930")
    private final String code;

    @Schema(description = "현재가", example = "59300")
    private final BigDecimal currentPrice;

    @Schema(description = "등락률 (%)", example = "-1.25")
    private final BigDecimal fluctuationRate;

    @Schema(description = "누적 거래량", example = "20837495")
    private final Long accumulatedVolume;

    public static MarketStockDto from(StockDetail stockDetail) {
        return MarketStockDto.builder()
                .id(stockDetail.getId())
                .name(stockDetail.getStocks().getStockName())
                .code(stockDetail.getStocks().getStockCode())
                .imageUrl(stockDetail.getStocks().getLogoUrl())
                .currentPrice(stockDetail.getClosePrice())
                .fluctuationRate(stockDetail.getPriceRate())
                .accumulatedVolume(stockDetail.getVolume())
                .build();
    }
}