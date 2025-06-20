package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class DynamicStockDataDto {

    @Schema(description = "종목 코드", example = "005930")
    private String code;

    @Schema(description = "현재가", example = "59300")
    private BigDecimal currentPrice;

    @Schema(description = "등락률 (%)", example = "-1.25")
    private BigDecimal fluctuationRate;

    @Schema(description = "누적 거래량", example = "2034590")
    private Long accumulatedVolume;

    public DynamicStockDataDto(StockDetail stockDetail) {
        this.code = stockDetail.getStockCode();
        this.currentPrice = stockDetail.getClosePrice();
        this.fluctuationRate = stockDetail.getPriceRate();
        this.accumulatedVolume = stockDetail.getVolume();
    }
}