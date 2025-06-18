package com.example.iotl.dto.realized;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class RealizedProfitDetailDto {
    private String stockName;
    private String type; // "판매수익" 또는 "배당금"
    private Long amount;
}