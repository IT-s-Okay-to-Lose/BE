package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
@EqualsAndHashCode(of = {"open", "high", "low", "close"})
public class CandleDataDto {
    @Schema(description = "시간 (ISO_LOCAL_DATE_TIME 포맷)", example = "2025-06-15T14:41:15")
    private final String time;

    @Schema(description = "시가", example = "60200")
    private final BigDecimal open;

    @Schema(description = "고가", example = "61000")
    private final BigDecimal high;

    @Schema(description = "저가", example = "59000")
    private final BigDecimal low;

    @Schema(description = "종가", example = "60500")
    private final BigDecimal close;

    public CandleDataDto(StockDetail stock) {
        this.time = stock.getCreatedAt().toString(); // 필요시 포맷 변경 가능
        this.open = stock.getOpenPrice();
        this.high = stock.getHighPrice();
        this.low = stock.getLowPrice();
        this.close = stock.getClosePrice();
    }

    public CandleDataDto(String time, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }
}