package com.example.iotl.dto;

import com.example.iotl.entity.StockDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
// 디테일 페이지 가격 관련 정보
@Getter
@Setter
@AllArgsConstructor
public class MarketStockPriceInfoDto {
    private BigDecimal currentPrice;
    private BigDecimal priceChange;
    private BigDecimal fluctuationRate;

    public MarketStockPriceInfoDto(StockDetail stockDetail) {
        this.currentPrice = stockDetail.getClosePrice();
        this.priceChange = stockDetail.getPriceDiff();
        this.fluctuationRate = stockDetail.getPriceRate();
    }
}