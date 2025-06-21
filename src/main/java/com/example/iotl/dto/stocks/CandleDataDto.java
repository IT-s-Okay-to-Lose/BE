package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class CandleDataDto {

    @Schema(description = "시간 (ISO_LOCAL_DATE_TIME 포맷)", example = "2025-06-15T14:41:15")
    private final String time;

    @Schema(description = "시가", example = "60200")
    private final double open;

    @Schema(description = "고가", example = "61000")
    private final double high;

    @Schema(description = "저가", example = "59000")
    private final double low;

    @Schema(description = "종가", example = "60500")
    private final double close;

    public CandleDataDto(StockDetail stock) {
        this.time = stock.getCreatedAt().toString(); // 필요시 포맷 변경 가능
        this.open = stock.getOpenPrice().doubleValue();
        this.high = stock.getHighPrice().doubleValue();
        this.low = stock.getLowPrice().doubleValue();
        this.close = stock.getClosePrice().doubleValue();
    }

    public CandleDataDto(String time, double open, double high, double low, double close) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
}