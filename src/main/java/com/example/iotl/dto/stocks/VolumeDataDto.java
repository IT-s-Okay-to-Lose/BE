package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class VolumeDataDto {

    @Schema(description = "데이터 생성 시각", example = "2025-06-16T09:15:00")
    private final String time;

    @Schema(description = "해당 시간의 거래량", example = "14500231")
    private final long volume;

    public VolumeDataDto(StockDetail stock) {
        this.time = stock.getCreatedAt().toString(); // ISO 8601 형식
        this.volume = stock.getVolume();
    }
}