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
        String volStr = output.getOrDefault("acml_vol", "0");
        long volume = 0L;
        try {
            volume = Long.parseLong(volStr.replaceAll(",", "").trim());
        } catch (NumberFormatException e) {
            // 로깅 또는 기본값 유지
            System.err.println("⚠️ 거래량 변환 실패: " + volStr);
        }

        return VolumeDataDto.builder()
                .time(LocalDateTime.now().toString())
                .volume(volume)
                .build();
    }
}