package com.example.iotl.dto.marketindex;

import com.example.iotl.entity.MarketIndex;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class MarketIndexDto {
    private String indexName;      // ex: 코스피
    private Double currentValue;   // bstp_nmix_prpr
    private Double changeAmount;   // bstp_nmix_prdy_vrss
    private Double changeRate;     // bstp_nmix_prdy_ctrt
    private String changeDirection;// prdy_vrss_sign → "▲", "▼"
}