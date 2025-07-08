package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class StockPriceDto {

    @Schema(description = "주식 상세 ID", example = "6")
    private final Long id;

    @Schema(description = "주식 종목 코드", example = "005930")
    private final String stockCode;

    @Schema(description = "시가", example = "70100.00")
    private final BigDecimal openPrice;

    @Schema(description = "고가", example = "71000.00")
    private final BigDecimal highPrice;

    @Schema(description = "저가", example = "69000.00")
    private final BigDecimal lowPrice;

    @Schema(description = "종가 (현재가)", example = "70300.00")
    private final BigDecimal closePrice;

    @Schema(description = "전일 대비 가격 차이", example = "-200.00")
    private final BigDecimal priceDiff;

    @Schema(description = "등락률 (%)", example = "-0.28")
    private final BigDecimal priceRate;

    @Schema(description = "전일 종가", example = "70500.00")
    private final BigDecimal prevClosePrice;

    @Schema(description = "등락 부호 (상승: 1, 하락: 5 등)", example = "5")
    private final Byte priceSign;

    @Schema(description = "누적 거래량", example = "21548977")
    private final Long volume;

    @Schema(description = "데이터 생성 시각", example = "2025-06-16T09:15:00")
    private final LocalDateTime createdAt;

    public static StockPriceDto from(StockDetail stock) {
        return StockPriceDto.builder()
                .id(stock.getId())
                .stockCode(stock.getStocks().getStockCode())
                .openPrice(stock.getOpenPrice())
                .highPrice(stock.getHighPrice())
                .lowPrice(stock.getLowPrice())
                .closePrice(stock.getClosePrice())
                .priceDiff(stock.getPriceDiff())
                .priceRate(stock.getPriceRate())
                .prevClosePrice(stock.getPrevClosePrice())
                .priceSign(stock.getPriceSign())
                .volume(stock.getVolume())
                .createdAt(stock.getCreatedAt())
                .build();
    }
}