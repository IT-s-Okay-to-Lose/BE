package com.example.iotl.controller;

import com.example.iotl.dto.CustomOAuth2User;
import com.example.iotl.dto.holding.MyStockSummaryDto;
import com.example.iotl.service.HoldingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class HoldingController {

    private final HoldingService holdingService;

    @Operation(summary = "내 주식 요약 조회", description = "현재 로그인한 유저가 보유한 특정 종목의 평균가, 수수료, 수익을 반환합니다.")
    @GetMapping("/{stockCode}")
    public MyStockSummaryDto getMyStockSummary(
        @Parameter(description = "종목 코드 (예: 005930)")
        @PathVariable String stockCode,
        @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        String userName = principal.getUsername();

        return holdingService.getMyStockSummary(userName, stockCode);
    }
}
