package com.example.iotl.controller;

import com.example.iotl.dto.dashboard.UserInvestmentSummaryDto;
import com.example.iotl.dto.holding.HoldingRatioDto;
import com.example.iotl.dto.realized.RealizedProfitDetailDateDto;
import com.example.iotl.dto.realized.RealizedProfitSummaryDto;
import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.entity.Holdings;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.global.response.BaseResponseService;
import com.example.iotl.repository.HoldingsRepository;
import com.example.iotl.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@CrossOrigin(origins = "*") // 또는 허용할 도메인만 지정
@Tag(name = "Dashboard/Summary API", description = "총 투자 요약 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final HoldingsRepository holdingsRepository;
    private final BaseResponseService baseResponseService;
    private String extractUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증 실패");
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUsername(); // 커스텀 구현에 따라 다를 수 있음
        } else {
            throw new RuntimeException("알 수 없는 사용자 정보");
        }
        return username.trim();
    }
    @Operation(
            summary = "총 투자 요약",
            description = "총 투자 요약과 ROI를 보여줍니다"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "원금, 총 이익, roi 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })

    @GetMapping("/summary")
    public ResponseEntity<BaseResponse<UserInvestmentSummaryDto>> getInvestmentSummary(Authentication authentication){
        String username = extractUsername(authentication);
        var result = dashboardService.getInvestmentSummary(username);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(result));
    }
    @Operation(
            summary = "도넛차트용 보유 종목 도넛 차트로 조회",
            description = "유저 아이디를 검색하여 유저가 보유한 종목 비율을 도넛 차트로 보여줍니다"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보유 종목 차트 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 발생")
    })
    @GetMapping("/holding-ratio")
    public ResponseEntity<BaseResponse<List<HoldingRatioDto>>> getHoldingRatio(Authentication authentication){
        String username = extractUsername(authentication);
        var result = dashboardService.getHoldingRatio(username);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(result));
    }

    @Operation(
            summary = "월별 실현 수익 요약 조회",
            description = "특정 사용자(userId)의 매도 체결 정보와 평균 매입가를 기반으로 해당 연도/월의 실현 수익을 계산해 반환합니다.<br/>"
                    + "연도(year)와 월(month)을 생략하면 현재 시점을 기준으로 조회됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "실현 수익 요약 조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 누락 또는 잘못된 형식"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/realized-summary")
    public ResponseEntity<BaseResponse<RealizedProfitSummaryDto>> getRealizedProfitSummary(
            Authentication authentication,
            @Parameter(description = "연도", example = "2025")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "월", example = "6")
            @RequestParam(required = false) Integer month
    ) {
        String username = extractUsername(authentication);
        if (year == null || month == null) {
            LocalDateTime now = LocalDateTime.now();
            year = now.getYear();
            month = now.getMonthValue();
        }
        var result = dashboardService.getRealizedProfitSummary(username, year, month);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(result));
    }
    @Operation(
            summary = "실현 수익 상세 내역 조회", description = "월별 판매수익 상세 리스트를 날짜별로 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "실현 수익 요약 조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 파라미터 누락 또는 잘못된 형식"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/realized-detail")
    public ResponseEntity<BaseResponse<List<RealizedProfitDetailDateDto>>> getRealizedProfitDetail(
            Authentication authentication,

            @Parameter(description = "연도", example = "2025")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "월", example = "6")
            @RequestParam(required = false) Integer month
    ) {
        String username = extractUsername(authentication);
        if (year == null || month == null) {
            LocalDateTime now = LocalDateTime.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        var result = dashboardService.getRealizedProfitDetail(username, year, month);
        return ResponseEntity.ok(baseResponseService.getSuccessResponse(result));
    }
}