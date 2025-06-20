package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// 디테일 페이지 가격 관련 정보 DTO
@Getter
@Setter
@AllArgsConstructor
public class MarketStockPriceInfoDto {

    @Schema(description = "현재가", example = "58300")
    private BigDecimal currentPrice;

    @Schema(description = "전일 대비 가격 변화", example = "-1200")
    private BigDecimal priceChange;

    @Schema(description = "등락률 (%)", example = "-2.02")
    private BigDecimal fluctuationRate;

    public MarketStockPriceInfoDto(StockDetail stockDetail) {
        this.currentPrice = stockDetail.getClosePrice();
        this.priceChange = stockDetail.getPriceDiff();
        this.fluctuationRate = stockDetail.getPriceRate();
    }
}