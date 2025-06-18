package com.example.iotl.service;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public UserInvestmentSummaryDto getInvestmentSummary(Long userId) {
        // TODO: 임시 하드코딩, 나중에 DB 연동
        Long totalCash = 10_000_000_000L;
        Long totalProfit = 2_000_000L;

        return new UserInvestmentSummaryDto(totalCash, totalProfit);
    }
}