package com.example.iotl.dto;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// 모의투자 보유 주식 정보 (Overview page)
@Getter
@Setter
@AllArgsConstructor
public class StockPortfolioDto {

    @Schema(description = "주식 상세 ID", example = "6")
    private Long id;

    @Schema(description = "종목 이미지 URL", example = "https://logo.clearbit.com/samsung.com")
    private String imageUrl;

    @Schema(description = "종목 이름", example = "삼성전자")
    private String name;

    @Schema(description = "종목 코드", example = "005930")
    private String code;

    @Schema(description = "보유 수량", example = "10")
    private int quantity;

    @Schema(description = "평균 매수가", example = "70000.00")
    private BigDecimal averagePrice;

    @Schema(description = "평가 금액 (현재가 * 수량)", example = "715000.00")
    private BigDecimal evaluatedPrice;

    @Schema(description = "등락률 (%)", example = "1.25")
    private BigDecimal fluctuationRate;

    public StockPortfolioDto(StockDetail stockDetail, int quantity, BigDecimal averagePrice) {
        this.id = stockDetail.getId();
        this.name = stockDetail.getStocks().getStockName();
        this.code = stockDetail.getStockCode();
        this.imageUrl = stockDetail.getStocks().getLogoUrl();
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.evaluatedPrice = stockDetail.getClosePrice().multiply(BigDecimal.valueOf(quantity));
        this.fluctuationRate = stockDetail.getPriceRate();
    }
}