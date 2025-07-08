package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import com.example.iotl.entity.Stocks;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class StockDetailDto {

    @Schema(description = "주식 상세 정보 ID", example = "6")
    private final Long id;

    @Schema(description = "종목 코드", example = "005930")
    private final String stockCode;

    @Schema(description = "시가", example = "71500.00")
    private final BigDecimal openPrice;

    @Schema(description = "고가", example = "72500.00")
    private final BigDecimal highPrice;

    @Schema(description = "저가", example = "71200.00")
    private final BigDecimal lowPrice;

    @Schema(description = "종가(현재가)", example = "71800.00")
    private final BigDecimal closePrice;

    @Schema(description = "전일 대비 가격 차이", example = "-300.00")
    private final BigDecimal priceDiff;

    @Schema(description = "등락률(%)", example = "-0.42")
    private final BigDecimal priceRate;

    @Schema(description = "누적 거래량", example = "15700000")
    private final Long volume;

    @Schema(description = "생성 시각", example = "2025-07-01T15:30:00")
    private final LocalDateTime createdAt;

    @Schema(description = "전일 종가", example = "72100.00")
    private final BigDecimal prevClosePrice;

    @Schema(description = "상승/하락/보합 구분", example = "5")
    private final Byte priceSign;

    // Entity → DTO 변환
    public static StockDetailDto from(StockDetail stock) {
        return StockDetailDto.builder()
                .id(stock.getId())
                .stockCode(stock.getStocks().getStockCode())
                .openPrice(stock.getOpenPrice())
                .highPrice(stock.getHighPrice())
                .lowPrice(stock.getLowPrice())
                .closePrice(stock.getClosePrice())
                .priceDiff(stock.getPriceDiff())
                .priceRate(stock.getPriceRate())
                .volume(stock.getVolume())
                .createdAt(stock.getCreatedAt())
                .prevClosePrice(stock.getPrevClosePrice())
                .priceSign(stock.getPriceSign())
                .build();
    }

    // Open API 응답 Map → DTO 변환
    public static StockDetailDto fromOutput(Map<String, String> output) {
        BigDecimal closePrice = new BigDecimal(output.get("stck_prpr"));
        BigDecimal priceDiff = new BigDecimal(output.get("prdy_vrss"));

        return StockDetailDto.builder()
                .stockCode(output.get("stck_shrn_iscd"))
                .openPrice(new BigDecimal(output.get("stck_oprc")))
                .highPrice(new BigDecimal(output.get("stck_hgpr")))
                .lowPrice(new BigDecimal(output.get("stck_lwpr")))
                .closePrice(closePrice)
                .priceDiff(priceDiff)
                .priceRate(new BigDecimal(output.get("prdy_ctrt")))
                .priceSign(Byte.parseByte(output.get("prdy_vrss_sign")))
                .volume(Long.parseLong(output.get("acml_vol")))
                .prevClosePrice(closePrice.subtract(priceDiff))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // DTO → Entity 변환
    public StockDetail toEntity(Stocks stocks) {
        return StockDetail.builder()
                .stocks(stocks)
                .openPrice(openPrice)
                .highPrice(highPrice)
                .lowPrice(lowPrice)
                .closePrice(closePrice)
                .priceDiff(priceDiff)
                .priceRate(priceRate)
                .priceSign(priceSign)
                .volume(volume)
                .prevClosePrice(prevClosePrice)
                .createdAt(createdAt)
                .build();
    }
}