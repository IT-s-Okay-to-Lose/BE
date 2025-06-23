package com.example.iotl.service;

import com.example.iotl.dto.holding.MyStockSummaryDto;

public interface HoldingService {

    MyStockSummaryDto getMyStockSummary(Long userId, String stockCode);
    MyStockSummaryDto getMyStockSummary(String userName, String stockCode);  // ⬅️ 이거 추가!


}
