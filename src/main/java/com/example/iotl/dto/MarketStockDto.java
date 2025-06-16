package com.example.iotl.dto;

import com.example.iotl.entity.StockDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// 메인 페이지 실시간 차트용
@Getter
@Setter
@AllArgsConstructor
public class MarketStockDto {
    private Long id;
    private String imageUrl;
    private String name;
    private String code;
    private BigDecimal currentPrice;
    private BigDecimal fluctuationRate;
    private Long accumulatedVolume;

    public MarketStockDto(StockDetail stockDetail) {
        this.id = stockDetail.getId();
        this.name = stockDetail.getStocks().getStockName();
        this.code = stockDetail.getStockCode();
        this.imageUrl = stockDetail.getStocks().getLogoUrl();
        this.currentPrice = stockDetail.getClosePrice();
        this.fluctuationRate = stockDetail.getPriceRate();
        this.accumulatedVolume = stockDetail.getVolume();
    }
}