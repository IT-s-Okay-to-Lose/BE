package com.example.iotl.dto;

import com.example.iotl.entity.StockDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

//디테일 페이지 종목 기본 정보
@Getter
@Setter
@AllArgsConstructor
public class MarketStockInfoDto {
    private Long id;
    private String imageUrl;
    private String name;
    private String code;

    public MarketStockInfoDto(StockDetail stockDetail) {
        this.id = stockDetail.getId();
        this.imageUrl = stockDetail.getStocks().getLogoUrl();
        this.name = stockDetail.getStocks().getStockName();
        this.code = stockDetail.getStockCode();
    }
}