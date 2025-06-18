package com.example.iotl.service;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
import com.example.iotl.dto.holding.HoldingRatioDto;

import java.util.List;


public interface DashboardService {
    UserInvestmentSummaryDto getInvestmentSummary(Long userId);
    List<HoldingRatioDto> getHoldingRatio(Long userId);
}

