package com.example.iotl.controller;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
import com.example.iotl.dto.holding.HoldingRatioDto;
import com.example.iotl.dto.realized.RealizedProfitDetailDateDto;
import com.example.iotl.dto.realized.RealizedProfitSummaryDto;
import com.example.iotl.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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
    @Operation(
            summary = "도넛차트용 보유 종목 도넛 차트로 조회",
            description = "유저 아이디를 검색하여 유저가 보유한 종목 비율을 도넛 차트로 보여줍니다"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보유 종목 차트 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/holding-ratio/{userId}")
    public List<HoldingRatioDto> getHoldingRatio(@PathVariable Long userId) {
        return dashboardService.getHoldingRatio(userId);
    }

    @Operation(
            summary = "월별 실현 수익 요약 조회",
            description = "특정 사용자(userId)의 매도 체결 정보와 평균 매입가를 기반으로 해당 연도/월의 실현 수익을 계산해 반환합니다. " +
                    "연도(year)와 월(month)을 생략하면 현재 시점을 기준으로 조회됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "실현 수익 요약 조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 누락 또는 잘못된 형식"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/realized-summary")
    public ResponseEntity<RealizedProfitSummaryDto> getRealizedProfitSummary(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        if (year == null || month == null) {
            LocalDateTime now = LocalDateTime.now();
            year = now.getYear();
            month = now.getMonthValue();
        }
        return ResponseEntity.ok(dashboardService.getRealizedProfitSummary(userId, year, month));
    }

    @Operation(summary = "실현 수익 상세 내역 조회", description = "월별 판매수익 상세 리스트를 날짜별로 반환합니다.")
    @GetMapping("/realized-detail")
    public ResponseEntity<List<RealizedProfitDetailDateDto>> getRealizedProfitDetail(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        if (year == null || month == null) {
            LocalDateTime now = LocalDateTime.now();
            year = now.getYear();
            month = now.getMonthValue();
        }
        return ResponseEntity.ok(dashboardService.getRealizedProfitDetail(userId, year, month));
    }
}