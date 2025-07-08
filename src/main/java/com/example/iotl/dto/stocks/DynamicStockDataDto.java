package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class DynamicStockDataDto {

    @Schema(description = "종목 코드", example = "005930")
    private final String code;

    @Schema(description = "현재가", example = "59300")
    private final BigDecimal currentPrice;

    @Schema(description = "등락률 (%)", example = "-1.25")
    private final BigDecimal fluctuationRate;

    @Schema(description = "누적 거래량", example = "2034590")
    private final Long accumulatedVolume;

    public static DynamicStockDataDto from(StockDetail stockDetail) {
        return DynamicStockDataDto.builder()
                .code(stockDetail.getStocks().getStockCode())
                .currentPrice(stockDetail.getClosePrice())
                .fluctuationRate(stockDetail.getPriceRate())
                .accumulatedVolume(stockDetail.getVolume())
                .build();
    }

    // DynamicStockDataDto.java
    public static DynamicStockDataDto from(Map<String, String> output, String code) {
        return DynamicStockDataDto.builder()
                .code(code)
                .currentPrice(new BigDecimal(output.get("stck_prpr")))
                .fluctuationRate(new BigDecimal(output.get("prdy_ctrt")))
                .accumulatedVolume(Long.parseLong(output.get("acml_vol")))
                .build();
    }
}