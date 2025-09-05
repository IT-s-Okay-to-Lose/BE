package com.example.iotl.dto.profit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfitResponse {
    private Long totalProfit;
    private List<ProfitPoint> points;
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProfitPoint{
        private String time;
        private Long amount;
    }
}
