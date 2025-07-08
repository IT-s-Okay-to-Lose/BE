package com.example.iotl.dto.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ExchangeSummaryDto {
    private double rate;
    private double difference;
    private double percent;

    public ExchangeSummaryDto(double rate, double difference, double percent) {
        this.rate = rate;
        this.difference = difference;
        this.percent = percent;
    }

    // 꼭 Getter 있어야 함 (Jackson serialization 위해)
    public double getRate() {
        return rate;
    }

    public double getDifference() {
        return difference;
    }

    public double getPercent() {
        return percent;
    }
}