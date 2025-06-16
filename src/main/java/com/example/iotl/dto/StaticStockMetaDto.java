package com.example.iotl.dto;

import com.example.iotl.entity.Stocks;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
// 변하지 않는 종목 정보 (코드, 이름, 이미지)
@Getter
@Setter
@AllArgsConstructor
public class StaticStockMetaDto {
    private Long id;
    private String code;
    private String name;
    private String imageUrl;

    public StaticStockMetaDto(Stocks stocks) {
        this.id = null; // StockDetail과 연결되면 설정 가능
        this.code = stocks.getStockCode();
        this.name = stocks.getStockName();
        this.imageUrl = stocks.getLogoUrl();
    }
}