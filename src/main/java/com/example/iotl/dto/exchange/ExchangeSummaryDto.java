package com.example.iotl.dto.exchange;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExchangeSummaryDto {
    private double rate;
    private double difference;
    private double percent;
}