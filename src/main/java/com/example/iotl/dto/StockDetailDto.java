package com.example.iotl.dto;

import com.example.iotl.entity.StockDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class StockDetailDto {
    private Long id; // 종목 코드(PK)
    private String name; // 종목 이름
    private String code; // 종목 코드
    private BigDecimal currentPrice; // 현재가
    private BigDecimal fluctuationRate; // 등락률
    private Long accumulatedVolume; // 누적 거래량
    private String imageUrl; // 종목 로고 이미지 Url
    public StockDetailDto(StockDetail stockDetail){
        this.id = stockDetail.getId();
        this.name = stockDetail.getStocks().getStockName();
        this.code = stockDetail.getStockCode();
        this.currentPrice = stockDetail.getClosePrice();
        this.fluctuationRate = stockDetail.getPriceRate();
        this.accumulatedVolume = stockDetail.getVolume();
        this.imageUrl = stockDetail.getStocks().getLogoUrl();
    }
}