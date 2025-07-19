package com.example.iotl.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountSummaryDto {
    private BigDecimal availableAmount;
    private BigDecimal investingAmount;
}
