package com.example.iotl.dto;

import com.example.iotl.entity.StockDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
// 실시간 갱신용 데이터만 따로
@Getter
@Setter
@AllArgsConstructor
public class DynamicStockDataDto {
    private Long id;
    private BigDecimal currentPrice;
    private BigDecimal fluctuationRate;
    private Long accumulatedVolume;

    public DynamicStockDataDto(StockDetail stockDetail) {
        this.id = stockDetail.getId();
        this.currentPrice = stockDetail.getClosePrice();
        this.fluctuationRate = stockDetail.getPriceRate();
        this.accumulatedVolume = stockDetail.getVolume();
    }
}