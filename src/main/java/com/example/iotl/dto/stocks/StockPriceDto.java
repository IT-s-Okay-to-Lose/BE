package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceDto {

    @Schema(description = "주식 상세 ID", example = "6")
    private Long id;

    @Schema(description = "주식 종목 코드", example = "005930")
    private String stockCode;

    @Schema(description = "시가", example = "70100.00")
    private BigDecimal openPrice;

    @Schema(description = "고가", example = "71000.00")
    private BigDecimal highPrice;

    @Schema(description = "저가", example = "69000.00")
    private BigDecimal lowPrice;

    @Schema(description = "종가 (현재가)", example = "70300.00")
    private BigDecimal closePrice;

    @Schema(description = "전일 대비 가격 차이", example = "-200.00")
    private BigDecimal priceDiff;

    @Schema(description = "등락률 (%)", example = "-0.28")
    private BigDecimal priceRate;

    @Schema(description = "전일 종가", example = "70500.00")
    private BigDecimal prevClosePrice;

    @Schema(description = "등락 부호 (상승: 1, 하락: 5 등)", example = "5")
    private Byte priceSign;

    @Schema(description = "누적 거래량", example = "21548977")
    private Long volume;

    @Schema(description = "데이터 생성 시각", example = "2025-06-16T09:15:00")
    private LocalDateTime createdAt;

    public StockPriceDto(StockDetail stock) {
        this.id = stock.getId();
        this.stockCode = stock.getStockCode();
        this.openPrice = stock.getOpenPrice();
        this.highPrice = stock.getHighPrice();
        this.lowPrice = stock.getLowPrice();
        this.closePrice = stock.getClosePrice();
        this.priceDiff = stock.getPriceDiff();
        this.priceRate = stock.getPriceRate();
        this.prevClosePrice = stock.getPrevClosePrice();
        this.priceSign = stock.getPriceSign();
        this.volume = stock.getVolume();
        this.createdAt = stock.getCreatedAt();
    }
}