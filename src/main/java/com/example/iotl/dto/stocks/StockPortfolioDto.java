package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class StockPortfolioDto {

    @Schema(description = "주식 상세 ID", example = "6")
    private final Long id;

    @Schema(description = "종목 이미지 URL", example = "https://logo.clearbit.com/samsung.com")
    private final String imageUrl;

    @Schema(description = "종목 이름", example = "삼성전자")
    private final String name;

    @Schema(description = "종목 코드", example = "005930")
    private final String code;

    @Schema(description = "보유 수량", example = "10")
    private final int quantity;

    @Schema(description = "평균 매수가", example = "70000.00")
    private final BigDecimal averagePrice;

    @Schema(description = "평가 금액 (현재가 * 수량)", example = "715000.00")
    private final BigDecimal evaluatedPrice;

    @Schema(description = "등락률 (%)", example = "1.25")
    private final BigDecimal fluctuationRate;

    public static StockPortfolioDto from(StockDetail stockDetail, int quantity, BigDecimal averagePrice) {
        return StockPortfolioDto.builder()
                .id(stockDetail.getId())
                .name(stockDetail.getStocks().getStockName())
                .code(stockDetail.getStocks().getStockCode())
                .imageUrl(stockDetail.getStocks().getLogoUrl())
                .quantity(quantity)
                .averagePrice(averagePrice)
                .evaluatedPrice(stockDetail.getClosePrice().multiply(BigDecimal.valueOf(quantity)))
                .fluctuationRate(stockDetail.getPriceRate())
                .build();
    }
}