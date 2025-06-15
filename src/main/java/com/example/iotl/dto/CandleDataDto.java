package com.example.iotl.dto;

import com.example.iotl.entity.StockDetail;
import lombok.Getter;

@Getter
public class CandleDataDto {
    private final String time; // ISO_LOCAL_DATE_TIME or formatted string
    private final double open;
    private final double high;
    private final double low;
    private final double close;

    public CandleDataDto(StockDetail stock) {
        this.time = stock.getCreatedAt().toString(); // 또는 custom 포맷
        this.open = stock.getOpenPrice().doubleValue();
        this.high = stock.getHighPrice().doubleValue();
        this.low = stock.getLowPrice().doubleValue();
        this.close = stock.getClosePrice().doubleValue();
    }
}