package com.example.iotl.controller;

import com.example.iotl.dto.holding.HoldingSummaryDto;
import com.example.iotl.dto.security.CustomOAuth2User;
import com.example.iotl.dto.holding.MyStockSummaryDto;
import com.example.iotl.global.response.BaseResponse;
import com.example.iotl.service.HoldingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

//    @GetMapping("/summary")
//    public List<HoldingSummaryDto> getMyHoldings(@AuthenticationPrincipal CustomOAuth2User principal) {
//        String username = principal.getUsername();
//        return holdingService.getMyHoldings(username);
//    }

    @Operation(summary = "내 보유 종목 전체 요약 조회", description = "현재 로그인한 유저가 보유한 모든 종목의 요약 정보를 반환합니다.")
    @GetMapping("/summary")
    public BaseResponse<List<HoldingSummaryDto>> getMyHoldings(
            @AuthenticationPrincipal CustomOAuth2User principal) {

        if (principal == null) {
            return BaseResponse.<List<HoldingSummaryDto>>builder()
                    .isSuccess(false)
                    .message("인증되지 않은 사용자입니다.")
                    .code(401)
                    .data(null)
                    .build();
        }

        String username = principal.getUsername(); // ✅ 여기!
        List<HoldingSummaryDto> result = holdingService.getHoldingsByUserName(username);

        return BaseResponse.<List<HoldingSummaryDto>>builder()
                .isSuccess(true)
                .message("보유 종목 요약 조회 성공")
                .code(200)
                .data(result)
                .build();
    }
}
