package com.example.iotl.dto.stocks;

import com.example.iotl.entity.StockDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Getter
@Builder
public class VolumeDataDto {

    @Schema(description = "데이터 생성 시각", example = "2025-06-16T09:15:00")
    private final String time;

    @Schema(description = "해당 시간의 거래량", example = "14500231")
    private final long volume;

    public static VolumeDataDto from(StockDetail stock) {
        return VolumeDataDto.builder()
                .time(stock.getCreatedAt().toString())
                .volume(stock.getVolume())
                .build();
    }

    public static VolumeDataDto from(Map<String, String> output) {
        return VolumeDataDto.builder()
                .time(LocalDateTime.now().toString())
                .volume(Long.parseLong(output.get("acml_vol")))
                .build();
    }
}