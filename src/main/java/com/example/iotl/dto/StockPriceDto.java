package com.example.iotl.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.iotl.entity.StockPrice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceDto {
    private Long id;
    private String stockCode;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal closePrice;
    private BigDecimal priceDiff;
    private BigDecimal priceRate;
    private BigDecimal prevClosePrice;
    private Byte priceSign;
    private Long volume;
    private LocalDateTime createdAt;

    public StockPriceDto(StockPrice stock) {
        this.id = stock.getId();
        this.stockCode = stock.getStockCode();
        this.openPrice = stock.getOpenPrice();
        this.highPrice = stock.getHighPrice();
        this.lowPrice = stock.getLowPrice();
        this.closePrice = stock.getClosePrice();
        this.priceDiff = stock.getPriceDiff();
        this.priceRate = stock.getPriceRate();
        this.prevClosePrice = stock.getPrevClosePrice();
        this.priceSign = stock.getPriceSign();
        this.volume = stock.getVolume();
        this.createdAt = stock.getCreatedAt();
    }
}