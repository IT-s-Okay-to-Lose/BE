package com.example.iotl.service;

import com.example.iotl.dto.holding.HoldingSummaryDto;
import com.example.iotl.dto.holding.MyStockSummaryDto;

import java.util.List;

public interface HoldingService {

//    MyStockSummaryDto getMyStockSummary(Long userId, String stockCode);
    MyStockSummaryDto getMyStockSummary(String userName, String stockCode);
    List<HoldingSummaryDto> getMyHoldings(String username);
    List<HoldingSummaryDto> getHoldingsByUserName(String name);
}
