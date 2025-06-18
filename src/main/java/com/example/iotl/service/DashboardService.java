package com.example.iotl.service;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;


public interface DashboardService {
    UserInvestmentSummaryDto getInvestmentSummary(Long userId);
}

