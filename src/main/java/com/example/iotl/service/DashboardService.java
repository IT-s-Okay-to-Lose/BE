package com.example.iotl.service;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
import com.example.iotl.dto.holding.HoldingRatioDto;
import com.example.iotl.dto.realized.RealizedProfitDetailDateDto;
import com.example.iotl.dto.realized.RealizedProfitSummaryDto;

import java.util.List;


public interface DashboardService {
    UserInvestmentSummaryDto getInvestmentSummary(String username);
    List<HoldingRatioDto> getHoldingRatio(String username);
    RealizedProfitSummaryDto getRealizedProfitSummary(String username, int year, int month);

    List<RealizedProfitDetailDateDto> getRealizedProfitDetail(String username, Integer year, Integer month);
}

