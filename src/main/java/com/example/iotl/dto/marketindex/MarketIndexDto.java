package com.example.iotl.dto.marketindex;

import com.example.iotl.entity.MarketIndex;
import lombok.*;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketIndexDto {
    private String indexName;
    private Double currentValue;
    private Double changeAmount;
    private Double changeRate;
    private String changeDirection;

    public static MarketIndexDto fromEntity(MarketIndex entity) {
        return MarketIndexDto.builder()
                .indexName(entity.getIndexName())
                .currentValue(entity.getCurrentValue())
                .changeAmount(entity.getChangeAmount())
                .changeRate(entity.getChangeRate())
                .changeDirection(entity.getChangeDirection())
                .build();
    }
}