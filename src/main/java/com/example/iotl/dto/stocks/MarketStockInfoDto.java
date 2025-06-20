package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// 디테일 페이지 종목 기본 정보 DTO
@Getter
@Setter
@AllArgsConstructor
public class MarketStockInfoDto {

    @Schema(description = "주식 상세 정보 ID", example = "1001")
    private Long id;

    @Schema(description = "종목 로고 이미지 URL", example = "https://logo.clearbit.com/samsung.com")
    private String imageUrl;

    @Schema(description = "종목 이름", example = "삼성전자")
    private String name;

    @Schema(description = "종목 코드", example = "005930")
    private String code;

    public MarketStockInfoDto(StockDetail stockDetail) {
        this.id = stockDetail.getId();
        this.imageUrl = stockDetail.getStocks().getLogoUrl();
        this.name = stockDetail.getStocks().getStockName();
        this.code = stockDetail.getStockCode();
    }
}