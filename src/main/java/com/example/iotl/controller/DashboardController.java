package com.example.iotl.controller;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
import com.example.iotl.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard/Summary API", description = "총 투자 요약 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    @Operation(
            summary = "총 투자 요약",
            description = "총 투자 요약과 ROI를 보여줍니다"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "원금, 총 이익, roi 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })

    @GetMapping("/summary")
    public ResponseEntity<UserInvestmentSummaryDto> getInvestmentSummary() {
        Long fakeUserId = 1L;
        return ResponseEntity.ok(dashboardService.getInvestmentSummary(fakeUserId));
    }
}