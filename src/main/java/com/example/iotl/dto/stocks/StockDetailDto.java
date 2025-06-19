package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class StockDetailDto {

    @Schema(description = "주식 상세 정보 ID", example = "6")
    private Long id;

    @Schema(description = "종목 이름", example = "삼성전자")
    private String name;

    @Schema(description = "종목 코드", example = "005930")
    private String code;

    @Schema(description = "현재가", example = "71500.00")
    private BigDecimal currentPrice;

    @Schema(description = "등락률(%)", example = "-1.25")
    private BigDecimal fluctuationRate;

    @Schema(description = "누적 거래량", example = "15700000")
    private Long accumulatedVolume;

    @Schema(description = "종목 로고 이미지 URL", example = "https://logo.clearbit.com/samsung.com")
    private String imageUrl;

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