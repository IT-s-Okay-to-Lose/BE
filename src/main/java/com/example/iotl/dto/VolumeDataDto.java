package com.example.iotl.dto;

import com.example.iotl.entity.StockDetail;
import lombok.Getter;

@Getter
public class VolumeDataDto {
    private final String time;
    private final long volume;

    public VolumeDataDto(StockDetail stock) {
        this.time = stock.getCreatedAt().toString();
        this.volume = stock.getVolume();
    }
}