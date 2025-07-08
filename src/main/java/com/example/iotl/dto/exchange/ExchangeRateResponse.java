package com.example.iotl.dto.exchange;

import lombok.Getter;

import java.util.Map;

@Getter
public class ExchangeRateResponse {
    private String base_code;
    private Map<String, Double> conversion_rates;
}