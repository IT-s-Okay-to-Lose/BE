package com.example.iotl.dto.holding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HoldingSummaryDto {
    private String stockName;
    private String stockImageUrl;
    private Integer quantity;
    private Double avgBuyPrice;
    private Double changeRate;
}
