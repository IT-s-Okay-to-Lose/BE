package com.example.iotl.service;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
import com.example.iotl.dto.holding.HoldingRatioDto;
import com.example.iotl.dto.realized.RealizedProfitSummaryDto;

import java.util.List;


public interface DashboardService {
    UserInvestmentSummaryDto getInvestmentSummary(Long userId);
    List<HoldingRatioDto> getHoldingRatio(Long userId);
    RealizedProfitSummaryDto getRealizedProfitSummary(Long userId, int year, int month);
}

