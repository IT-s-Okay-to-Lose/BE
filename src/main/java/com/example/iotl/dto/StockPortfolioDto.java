package com.example.iotl.dto;

import com.example.iotl.entity.StockDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
// 모의투자 보유 주식 정보 (Overview page)
@Getter
@Setter
@AllArgsConstructor
public class StockPortfolioDto {
    private Long id;
    private String imageUrl;
    private String name;
    private String code;
    private int quantity;
    private BigDecimal averagePrice;
    private BigDecimal evaluatedPrice;
    private BigDecimal fluctuationRate;

    public StockPortfolioDto(StockDetail stockDetail, int quantity, BigDecimal averagePrice) {
        this.id = stockDetail.getId();
        this.name = stockDetail.getStocks().getStockName();
        this.code = stockDetail.getStockCode();
        this.imageUrl = stockDetail.getStocks().getLogoUrl();
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.evaluatedPrice = stockDetail.getClosePrice().multiply(BigDecimal.valueOf(quantity));
        this.fluctuationRate = stockDetail.getPriceRate();
    }
}