package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Builder
public class MarketStockPriceInfoDto {

    @Schema(description = "현재가", example = "58300")
    private final BigDecimal currentPrice;

    @Schema(description = "전일 대비 가격 변화", example = "-1200")
    private final BigDecimal priceChange;

    @Schema(description = "등락률 (%)", example = "-2.02")
    private final BigDecimal fluctuationRate;

    public static MarketStockPriceInfoDto from(StockDetail stockDetail) {
        return MarketStockPriceInfoDto.builder()
                .currentPrice(stockDetail.getClosePrice())
                .priceChange(stockDetail.getPriceDiff())
                .fluctuationRate(stockDetail.getPriceRate())
                .build();
    }

    public static MarketStockPriceInfoDto from(Map<String, String> output) {
        return MarketStockPriceInfoDto.builder()
                .currentPrice(new BigDecimal(output.get("stck_prpr")))
                .priceChange(new BigDecimal(output.get("prdy_vrss")))
                .fluctuationRate(new BigDecimal(output.get("prdy_ctrt")))
                .build();
    }
}