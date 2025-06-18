package com.example.iotl.controller;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
import com.example.iotl.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<UserInvestmentSummaryDto> getInvestmentSummary() {
        Long fakeUserId = 1L;
        return ResponseEntity.ok(dashboardService.getInvestmentSummary(fakeUserId));
    }
}